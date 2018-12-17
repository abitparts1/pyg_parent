package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.CookieUtil;
import com.pyg.cart.service.CartService;
import com.pyg.entity.Result;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 50000)
    private CartService cartService;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpServletRequest request;

    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //判断是否登录
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登用户：" + name);

        String cartList_String = CookieUtil.getCookieValue(request, "cartList", "utf-8");

        if(StringUtils.isEmpty(cartList_String)){
            cartList_String = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList_String, Cart.class);
        if("anonymousUser".equals(name)){
            //没有登录， 操作cookie
            //1. 获取cookie中的购物车列表
            return cartList_cookie;

        }else{
            //登录了，查询redis中购物车列表，返回
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);

            //购物车的合并操作
            if(cartList_cookie.size() > 0){
                try {
                    //1. 离线购物车中有商品信息，才进行合并
                    cartList_redis = cartService.mergeCartList(cartList_cookie,cartList_redis);

                    //2.清空cookie中的购物车列表信息
                    CookieUtil.deleteCookie(request,response,"cartList");

                    //3.将合并后的购物车列表重新写回到redis中
                    cartService.saveCartListToRedis(name,cartList_redis);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return  cartList_redis;

        }

    }
    /**
     * 添加商品到购物车列表
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCartList(Long itemId,Integer num){

        //1.获取登陆用户名判断是否登陆了
        try {
            //处理js的跨越问题,允许http://localhost:9105跨域资源调用
            //response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
            //允许跨域调用的浏览器携带cookie，服务端会进行处理
            //response.setHeader("Access-Control-Allow-Credentials","true");
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username.equals("anonymousUser")) {
                //登陆了就操作cookie，把商品添加到cookie中
                List<Cart> cartList = findCartList();
                //2.调用服务层把将要添加的商品保存到购物车中
                cartList = cartService.addGoodsToCartList(cartList, itemId, num);
                //3.将保存后的数据重新写入到cookie中
                String cartList_json = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartList_json, 3600 * 24 * 7, "utf-8");
            }else {
                //如果登陆了,查询redis中的购物车列表，把要添加的商品添加到购物车再写回redis中
                List<Cart> cartList_redis = findCartList();
                //调用服务层添加商品
                cartList_redis = cartService.addGoodsToCartList(cartList_redis,itemId,num);
                //将添加后的购物车再保存到redis中
                cartService.saveCartListToRedis(username,cartList_redis);


            }
            return new Result(true,"添加成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }


}
