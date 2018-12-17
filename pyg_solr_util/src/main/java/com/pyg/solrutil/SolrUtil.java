package com.pyg.solrutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import com.pyg.pojo.TbItemExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    //导入商品数据至索引库
    public void importItemData(){
        //根据状态码查询审核通过的商品列表
        TbItemExample example = new TbItemExample();
        Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);
        //根据商品列表获取商品标题信息
        System.out.println("=====商品列表=====");
        for (TbItem item : itemList) {
            System.out.println(item.getTitle());
            Map specMap = JSON.parseObject(item.getSpec());
            item.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("=======结束=======");
    }

    public static void main(String[] args) {
        //启动spring容器
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = context.getBean(SolrUtil.class);
        //调用方法
        solrUtil.importItemData();
    }
}
