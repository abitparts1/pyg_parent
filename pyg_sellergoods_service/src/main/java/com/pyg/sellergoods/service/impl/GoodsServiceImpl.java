package com.pyg.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pyg.entity.Goods;
import com.pyg.mapper.*;
import com.pyg.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.pojo.TbGoodsExample.Criteria;
import com.pyg.sellergoods.service.GoodsService;
import com.pyg.entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	public TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbGoods goods) {

		goodsMapper.insert(goods);

	}
	private void insertItem(Goods goods){
		if ("1".equals(goods.getGoods().getIsEnableSpec())){
			for (TbItem item:goods.getItemCatList()) {
				//获取spu+规格组合  iphonex  16G  联通3G
				String title = goods.getGoods().getGoodsName();
				//获取{"机身内存":"16G","网络":"联通3G"}是json格式需要转换
				Map<String,String> spec = JSON.parseObject(item.getSpec(), Map.class);
				for (String key:spec.keySet()){
					title+=" "+spec.get(key);
				}
				item.setTitle(title);
				setItem(goods,item);
				itemMapper.insert(item);
			}
		}else {//不启用规格，只用一条SKU
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			setItem(goods,item);
			item.setPrice(goods.getGoods().getPrice());
			item.setNum(9999);
			item.setStatus("1");
			item.setIsDefault("1");
			item.setSpec("{ }");
			itemMapper.insert(item);
		}
	}
	private void setItem(Goods goods,TbItem item){
		//获取第一个图片的url
		List<Map> images = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		//获取到后只取url的value属性添加到item临时对象内
		if (images.size()>0){
			item.setImage((String)images.get(0).get("url"));
		}
		//设置Id为商品表第三级分类的id
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());
		item.setUpdateTime(new Date());
		//获取基本信息表Id和商家信息表（获取商家登陆名）
		item.setGoodsId(goods.getGoods().getId());
		item.setSellerId(goods.getGoods().getSellerId());
		//设置分类名称
		item.setCategory(itemCatMapper.selectByPrimaryKey(item.getCategoryid()).getName());
		//品牌名称
		item.setBrand(brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId()).getName());
		//店铺名称
		item.setSeller(sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId()).getNickName());
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改基本信息
		goods.getGoods().setAuditStatus("0");//被修改的信息状态需要重新设置为0
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//修改扩展信息
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//修改SKU列表，需要先删除数据库的
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//重新关联设置页面
		insertItem(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//查询基本信息
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//查询扩展信息
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//查询详细信息
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemCatList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		//查询没被删除的商品
			criteria.andIsDeleteIsNull();
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Goods goods) {
		//添加商品表tb_goods，添加时前端会传入数据，后端把状态码设置为默认值再加入到新对象中
		goods.getGoods().setAuditStatus("0");//设置未审核状态
		goodsMapper.insert(goods.getGoods());
		//同上
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置新传入的Id
		goodsDescMapper.insert(goods.getGoodsDesc());
		//新增SKU信息
		insertItem(goods);
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
}

	@Override
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodIds, String status) {
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		//审核通过
		criteria.andStatusEqualTo("1");
		criteria.andGoodsIdIn(Arrays.asList(goodIds));
		return itemMapper.selectByExample(example);
	}

}
