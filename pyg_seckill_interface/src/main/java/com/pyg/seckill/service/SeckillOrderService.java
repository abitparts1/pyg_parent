package com.pyg.seckill.service;

import com.pyg.entity.PageResult;
import com.pyg.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {


	//读取秒杀订单
	public TbSeckillOrder searchSeckillOrderFromRedis(String username);


	/**
	 * 从缓存中删除订单
	 * @param username
	 * @param out_trad_no
	 */
	public void deleteOrderFromRedis(String username, String out_trad_no);

	/**
	 * 支付成功保存订单
	 * @param username
	 * @param orderId
	 */

	public void saveOrderFromRedisToDb(String username,Long orderId,String transactionId);

	/**
	 * 根据用户名查询秒杀订单
	 * @param username
	 */

	public TbSeckillOrder searchOrderFromRedisByUserName(String username);


	/**
	 * 提交订单
	 * @param seckillId
	 * @param username
	 */

	public void submitOrder(Long seckillId,String username);

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
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
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	void updateOrderStatus(String out_trade_no, String username, String transaction_id) throws Exception;
}
