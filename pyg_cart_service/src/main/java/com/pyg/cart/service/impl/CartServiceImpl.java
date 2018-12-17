package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service(timeout = 50000)
public class CartServiceImpl implements CartService{

    @Autowired
    private TbItemMapper itemMapper;
    /**
     * 添加商品到购物车列表
     *
     * @param cartList 添加前的购物车列表
     * @param itemId   要添加的商品skuid
     * @param num      添加的数量
     * @return 添加商品后的购物车列表
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num){
        //1.根据商品SKU ID查询SKU商品信息
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);

        //2.获取商家ID
        String sellerName = tbItem.getSeller();
        String sellerId = tbItem.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartFromCartList(sellerId,cartList);

        //4. 如果购物车列表中不存在该商家的购物车
        if(cart == null){
            //4.1 创建购物车，同时将商品添加到购物车中
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(sellerName);

            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(tbItem,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2 将新建的购物车添加到购物车列表中
            cartList.add(cart);

        }else{
            //5. 存在该商家的购物车

            //6. 查询该购物车中购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);

            if(orderItem == null){
                //6.1  如果没有，新增购物车明细,同时添加到购物明细列表中
                orderItem = createOrderItem(tbItem,num);
                cart.getOrderItemList().add(orderItem);


            }else {
                //6.2  如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);

                //重新计算小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));


                //移除的逻辑
                if(orderItem.getNum() <= 0){
                    //从购物明细列表中移除该商品
                    cart.getOrderItemList().remove(orderItem);
                }

                if(cart.getOrderItemList().size() == 0){
                    //从cartList中移除该cart
                    cartList.remove(cart);
                }

            }

        }


        return cartList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        //获取用户个人购物车信息，如果不为空就返回购物车列表
        Object cartList = redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            //如果没有则创建一个购物车列表
            cartList = new ArrayList<>();
        }
        return (List<Cart>) cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList_redis) {
        //将用户的个人购物车列表保存到redis中
        redisTemplate.boundHashOps("cartList").put(username,cartList_redis);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {
        System.out.println("合并购物车");
        //遍历cookie购物车列表，再遍历明细列表，调用添加商品方法添加到购物车1
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem orderItem:cart.getOrderItemList()){
                cartList_redis=addGoodsToCartList(cartList_redis,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList_redis;
    }

    /**
     * 查询该购物车中购物车明细列表中是否存在该商品
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 根据商家ID判断购物车列表中是否存在该商家的购物车
     * @param sellerId 商家id
     * @param cartList 购物车列表
     * @return
     */
    private Cart searchCartFromCartList(String sellerId, List<Cart> cartList) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return  null;
    }


    private TbOrderItem createOrderItem(TbItem tbItem,Integer num){
        //创建购物车明细对象
        TbOrderItem orderItem = new TbOrderItem();
        //数量
        orderItem.setGoodsId(tbItem.getGoodsId());
        orderItem.setNum(num);
        orderItem.setPicPath(tbItem.getImage());
        orderItem.setItemId(tbItem.getId());
        //单价
        orderItem.setPrice(tbItem.getPrice());
        orderItem.setTitle(tbItem.getTitle());
        orderItem.setSellerId(tbItem.getSellerId());
        //小计
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*num));
        return orderItem;
    }
}

