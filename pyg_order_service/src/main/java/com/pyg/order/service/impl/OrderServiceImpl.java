package com.pyg.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.IdWorker;
import com.pyg.entity.PageResult;
import com.pyg.mapper.TbOrderItemMapper;
import com.pyg.mapper.TbOrderMapper;
import com.pyg.mapper.TbPayLogMapper;
import com.pyg.order.service.OrderService;
import com.pyg.pojo.TbOrder;
import com.pyg.pojo.TbOrderExample;
import com.pyg.pojo.TbOrderExample.Criteria;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojo.TbPayLog;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import sun.rmi.runtime.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service(timeout = 50000)
public class OrderServiceImpl implements OrderService{

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbPayLogMapper payLogMapper;

	//根据订单号和交易流水号修改订单状态
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//1.修改日志状态
		// 1.1获取一条支付日志，设置内容
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setPayTime(new Date());//支付时间
		payLog.setTransactionId(transaction_id);//交易流水号
		payLog.setTradeState("1");//已支付
		payLogMapper.updateByPrimaryKey(payLog);
		//2.修改订单状态
		String orderList = payLog.getOrderList();//订单列表
		String[] orderIds = orderList.split(",");//切割后的订单列表
		for (String orderId : orderIds) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if (tbOrder!=null){
				tbOrder.setStatus("2");//已付款
				tbOrder.setPaymentTime(new Date());
				orderMapper.updateByPrimaryKey(tbOrder);
			}
		}
		//清楚redis中的缓存
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {

		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//获取购物车数据
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		//遍历购物车给订单商品设置ID
		List<Long> orderList = new ArrayList<>();//订单ID列表
		double totalFee = 0;//总金额
		for (Cart cart : cartList) {
			long orderId = idWorker.nextId();
			System.out.println("orderId:"+orderId);
			order.setOrderId(orderId);
			order.setCreateTime(new Date());
			order.setUpdateTime(new Date());
			order.setSellerId(cart.getSellerId());
			//状态 未支付
			order.setStatus("1");
			//添加订单到父订单中
			orderList.add(orderId);
			//总金额
			double payment=0.0;
			//一个订单会有多个订单明细
			for (TbOrderItem orderItem : cart.getOrderItemList()){
				//设置订单明细ID
				long orderItemId = idWorker.nextId();

				orderItem.setId(orderItemId);

				orderItem.setOrderId(orderId);

				//计算实际金额
				payment+=orderItem.getTotalFee().doubleValue();
				//保存到数据库
				orderItemMapper.insert(orderItem);
			}
			order.setPayment(new BigDecimal(payment));

			orderMapper.insert(order);
		}
		//业务逻辑补充
		if (order.getPaymentType().equals("1")){
			//微信支付
			TbPayLog payLog = new TbPayLog();
			//给父订单赋ID值
			String out_trade_no = idWorker.nextId()+"";
			//设置信息
			payLog.setOutTradeNo(out_trade_no);
			payLog.setCreateTime(new Date());
			Long totalFee_long = (long)(totalFee*100);
			payLog.setTotalFee(totalFee_long);

			// 0 未支付
			payLog.setTradeState("0");
			payLog.setUserId(order.getUserId());
			//设置支付类型
			payLog.setPayType(order.getPaymentType());
			String ids = orderList.toString().replace("[","").replace("]","").replace(" ","");
			payLog.setOrderList(ids);
			//保存到数据库
			payLogMapper.insert(payLog);
			//保存到redis
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
			System.out.println("此订单已经支付过了");
		}
		//3.清除购物车中的商品信息
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
