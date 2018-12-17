package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.IdWorker;
import com.pyg.entity.Result;
import com.pyg.pay.service.WeChatPayService;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("pay")
public class payController {

    @Reference
    private WeChatPayService weChatPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @Autowired
    private IdWorker idWorker;


    //生成微信二维码

    @RequestMapping("createNative")
    public Map createNative(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("获取到登陆名订单");
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserName(username);
        //获取订单
        if (seckillOrder==null){
            throw new RuntimeException("不存在订单");
        }
        return weChatPayService.createNative(seckillOrder.getId()+"",(long)(seckillOrder.getMoney().doubleValue()*100)+"");
    }


    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Result result = null;
        int x = 0;
        try {

            while (true){
                //调用查询服务
                Map map = weChatPayService.queryPayStatus(out_trade_no);
                if (map==null){
                    //查询失败
                    break;
                }
                if (map.get("trade_state").equals("SUCCESS")){

                    //支付成功后调用修改状态的方法
                    // orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));
                    //成功
                    result= new Result(true,"支付成功");
                    //保存到数据库
                    seckillOrderService.saveOrderFromRedisToDb(username,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                    break;
                }
                //间隔三秒查询一次
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                x++;
                if (x>=20){
                    result=new Result(false,"PAY_TIME_OUT");
                    //调用微信关闭支付接口
                    Map<String,String> close_result = weChatPayService.closePay(out_trade_no);
                    if ("FAIL".equals(close_result.get("return_code"))){
                        //微信订单关闭不成功
                        if ("ORDERPAID".equals(close_result.get("err_code"))){
                            //用户支付成功
                            result = new Result(true,"支付成功");
                            seckillOrderService.updateOrderStatus(out_trade_no,username, (String) map.get("transaction_id"));

                        }
                    }
                }
                if (!result.isSuccess()){
                    //真的超时了
                    System.out.println("超时，取消订单");
                    seckillOrderService.deleteOrderFromRedis(username,out_trade_no);
                }

            }
        }catch (Exception e){
            e.printStackTrace();
            result = new Result(true,"支付失败");
        }
        return result;
    }
}
