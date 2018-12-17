package com.pyg.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.IdWorker;
import com.pyg.entity.PageResult;
import com.pyg.mapper.TbSeckillGoodsMapper;
import com.pyg.mapper.TbSeckillOrderMapper;
import com.pyg.pojo.TbSeckillGoods;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.pojo.TbSeckillOrderExample;
import com.pyg.pojo.TbSeckillOrderExample.Criteria;
import com.pyg.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;


	//读取秒杀订单
	@Override
	public TbSeckillOrder searchSeckillOrderFromRedis(String username) {

		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
	}

	//根据用户名和订单Id删除订单
	@Override
	public void deleteOrderFromRedis(String username, String out_trad_no) {
		//获取订单信息
	/*	TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
		if (seckillOrder!=null&&seckillOrder.getId().longValue()==Long.valueOf(out_trad_no)){
			//如果订单不为空且与传递的订单Id可对应上，则删除缓存
			redisTemplate.boundHashOps("seckillOrder").delete(username);
			//再恢复缓存,从缓存中提取秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getId());

			if (seckillGoods!=null){
				//再对缓存中商品进行加一还原
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
				//再保存到缓存中
				redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);
			}*/

		//1. 读取秒杀订单
		TbSeckillOrder seckillOrder = searchSeckillOrderFromRedis(username);

		//2.恢复库存
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());

		seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);

		redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(),seckillGoods);


		//3.删除redis中 的秒杀订单
		redisTemplate.boundHashOps("seckillOrder").delete(username);
	}



	//根据用户名商品Id交易流水号保存订单
	@Override
	public void saveOrderFromRedisToDb(String username, Long orderId, String transactionId) {
		//获取订单信息并设置信息为支付状态
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
		System.out.println(seckillOrder);
		//判断订单是否正常
		if (seckillOrder==null){
			throw  new RuntimeException("此订单不存在");
		}
		//判断传递过来的订单号是否符合缓存中的订单号
		if (seckillOrder.getId().longValue()!=orderId){
			throw new RuntimeException("订单号不匹配");
		}
		//修改为支付成功的状态
		seckillOrder.setTransactionId(transactionId);
		seckillOrder.setPayTime(new Date());
		seckillOrder.setStatus("2");//已支付
		//保存到数据库并从redis中清除
		seckillOrderMapper.insert(seckillOrder);
		redisTemplate.boundHashOps("seckillOrder").delete(username);

	}

	//根据用户名查询秒杀订单
	@Override
	public TbSeckillOrder searchOrderFromRedisByUserName(String username) {
		//获取用户的秒杀订单
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(username);
	}

	//商家根据商品Id提交订单
	@Override
	public void submitOrder(Long seckillId, String username) {
		//获取缓存中的商品信息,给订单赋值ID，设置状态和字段提交保存到Redis中
		//获取redis中的商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		//判断商品是否是空
		if (seckillGoods==null){
			throw  new RuntimeException("商品不存在");
		}
		if (seckillGoods.getStockCount()<=0){
			throw new RuntimeException("商品已抢购一空");
		}
		//扣减redis库存，再放回库存，如果为零则同步信息到数据库，删除缓存
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
		if (seckillGoods.getStockCount()==0){
			//商品被抢光
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			//删除缓存
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
		}
		//设置订单状态，与字段,先设置订单的ID
		long orderId = idWorker.nextId();
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(orderId);
		seckillOrder.setStatus("0");//未支付
		seckillOrder.setSeckillId(seckillId);
		seckillOrder.setUserId(username);
		seckillOrder.setMoney(seckillGoods.getCostPrice());//总金额
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setCreateTime(new Date());

		//4. 将生成的订单放到redis中
		redisTemplate.boundHashOps("seckillOrder").put(username,seckillOrder);
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String username, String transaction_id) throws Exception {
		//1.读取redis中的秒杀订单

		TbSeckillOrder seckillOrder = searchSeckillOrderFromRedis(username);

		//2. 修改状态和支付时间.流水号
		if(seckillOrder == null){
			throw  new Exception("秒杀订单不存在");
		}
		seckillOrder.setStatus("1");//已支付
		seckillOrder.setPayTime(new Date());
		seckillOrder.setTransactionId(transaction_id);

		//3. 保存到数据库中
		seckillOrderMapper.insert(seckillOrder);

		//4. 销毁redis中的秒杀订单
		redisTemplate.boundHashOps("seckillOrder").delete(username);
	}

}
