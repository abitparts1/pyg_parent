package com.pyg.cart.service;

import com.pyg.pojogroup.Cart;

import java.util.List;

public interface CartService {

    //添加到购物车
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    //根据用户名查询redis中的购物车列表
    public List<Cart> findCartListFromRedis(String username);
    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList_redis
     */
    void saveCartListToRedis(String username,List<Cart> cartList_redis);

    /**
     * 合并购物车
     * @param cartList_cookie
     * @param cartList_redis
     * @return
     */
   public List<Cart> mergeCartList(List<Cart> cartList_cookie,List<Cart> cartList_redis);
}
