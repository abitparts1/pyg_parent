package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout = 3000)
public class ItemSearchServiceImpl implements ItemSearchService{

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map<String,Object> resultMap = new HashMap<>();
//------关键字的处理
        String keywords = (String) searchMap.get("keywords");
        keywords = keywords.replace(" ","");
        searchMap.put("keywords",keywords);
//------高亮查询
        Map mapHighlight = searchHighlightQuery(searchMap);
        resultMap.putAll(mapHighlight);
//------分组查询
        Map categoryMap = searchCategoryList(searchMap);
        resultMap.putAll(categoryMap);
//------根据商品名称查询商品和规格列表
       //获取第一个商品分类
        Object category = searchMap.get("category");
        if (category==null||"".equals(category)){//前端没有传递商品参数，默认从第一个开始查询
            List<String> categoryList = (List<String>) categoryMap.get("categoryList");
            if (categoryList.size()>0){
            category = categoryList.get(0);
            }
        }
        Map searchBrandSpecMap = searchBrangListAndSpecList((String) category);
        resultMap.putAll(searchBrandSpecMap);
        return resultMap;
    }

    //把审核通过的商品表导入到Solr索引库
    @Override
    public void importList(List itemList) {
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        //根据条件删除，前提是删除选项在商品列表里
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //根据分类名称查询品牌和规格列表
    private Map searchBrangListAndSpecList(String category) {
        Map resultMap = new HashMap<>();
        //获取类型模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        //根据模板ID查询品牌列表
        Object brandList = redisTemplate.boundHashOps("brandList").get(typeId);
        //获取规格Id
        Object specList = redisTemplate.boundHashOps("specList").get(typeId);
        //返回给前端
        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;
    }

    private Map searchHighlightQuery(Map searchMap) {

        Map<String,Object> resultMap = new HashMap<>();
        String keywords = (String) searchMap.get("keywords");
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮字段
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");
        //封装高亮选项
        query.setHighlightOptions(highlightOptions);
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
//----------------------------------------过滤条件----------------------------------------------------
       //=======分类条件过滤
         //获取参数
        Object category = searchMap.get("category");
        //封装过滤条件指定字段
        if (category!=null&& StringUtils.isNotEmpty((String)category)){
        Criteria filterCriteria = new Criteria("item_category").is(category);
        FilterQuery queryFilter = new SimpleFilterQuery(filterCriteria);
        query.addFilterQuery(queryFilter);
        }
       //=======品牌过滤条件
        //获取参数
        Object brand = searchMap.get("brand");
        System.out.println(brand);
        if (brand!=null&&StringUtils.isNotEmpty((String)brand)){
            Criteria filterCriteria = new Criteria("item_brand").is(brand);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
       //====规格信息过滤查询，因为规格是多值的json
        if (searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(searchMap.get(key));
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
      //=====按价格筛选过滤
        String price = (String) searchMap.get("price");
        if (price!=null&&StringUtils.isNotEmpty(price)){
            String[] priceStr = price.split("-");
            if (!priceStr[0].equals(0)){//如果区间起点不等于0
            Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceStr[0]);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
            }
            if (!priceStr[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceStr[1]);
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
    //=====分页处理
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");
        //开始索引
        query.setOffset((pageNo-1)*pageSize);
        //每页显示条数
        query.setRows(pageSize);

    //=====排序的处理
        Object sortField = searchMap.get("sortField");//排序字段
        Object sorts = searchMap.get("sort");//ASC  DESC
        if (sortField!=null&&StringUtils.isNotEmpty((String)sortField)){
            if ("ASC".equals(sorts)){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sorts)){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

//---------------------------------------高亮查询------------------------------------------------------
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //获取高亮显示内容进行替换
        //取高亮的入口集合
        List<HighlightEntry<TbItem>> highlightEntries = highlightPage.getHighlighted();
        for (HighlightEntry<TbItem> highlightEntry : highlightEntries) {
            TbItem tbItem = highlightEntry.getEntity();
            List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
            for (HighlightEntry.Highlight highlight : highlights) {
                List<String> sns = highlight.getSnipplets();
                String title = sns.get(0);
                System.out.println(title);
                tbItem.setTitle(title);
            }
        }
        List<TbItem> content = highlightPage.getContent();
        resultMap.put("rows",content);
        //返回分页总记录数，总页数
        long totalElements = highlightPage.getTotalElements();
        resultMap.put("total",totalElements);
        int totalPages = highlightPage.getTotalPages();
        resultMap.put("totalPages",totalPages);
        return resultMap;
    }

    private  Map searchCategoryList(Map searchMap){
        Map<String, Object> resultMap = new HashMap<>();
        //根据关键字查询商品分类面板
        List<String> list = new ArrayList<>();
        SimpleQuery query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //封装查询条件
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据分页得到列的结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //根据结果集得到分页集合入口
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //再获取内容遍历显示
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entity : content) {
            list.add(entity.getGroupValue());
        }
        resultMap.put("categoryList",list);
        return resultMap;

    }
}
