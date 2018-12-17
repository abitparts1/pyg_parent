package com.pyg.sellergoods.service;
import java.util.List;

import com.pyg.entity.Goods;
import com.pyg.pojo.TbGoods;

import  com.pyg.entity.PageResult;
import com.pyg.pojo.TbItem;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface GoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbGoods goods);
	
	
	/**
	 * 修改
	 */
	public void update(Goods goods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public Goods findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

	//添加商品，商品扩展，商品详情
	public void add(Goods goods);
	//商品审核批量修改状态
	public void updateStatus(Long[] ids,String status);
	//根据商品和状态码
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodIds, String status);
}
