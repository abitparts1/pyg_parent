package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.IdWorker;
import com.pyg.entity.Result;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeChatPayService;
import com.pyg.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("pay")
public class PayController {

    @Reference(timeout = 50000)
    private WeChatPayService weChatPayService;

    @Reference
    private OrderService orderService;
    @Autowired
    private IdWorker idWorker;


    @RequestMapping("createNative")
    public Map createNative(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if (payLog!=null){
        String out_trade_no=idWorker.nextId()+"";
        String total_fee="1";
        return weChatPayService.createNative(out_trade_no,total_fee);
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        int x = 0;
        while (true){
            //调用查询服务
            Map map = weChatPayService.queryPayStatus(out_trade_no);
            if (map==null){
                //查询失败
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){

                //支付成功后调用修改状态的方法
                orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));
                //成功
                return new Result(true,"支付成功");
            }
            //间隔三秒查询一次
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x>=100){
                result=new Result(false,"PAY_TIME_OUT");
                break;
            }

        }
        return result;
    }
}
