package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.HttpClient;
import com.pyg.pay.service.WeChatPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatPayServiceImpl implements WeChatPayService{
    //生成二维码四个参数，公众号ID，商户号ID，密钥，回调函数URL
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String key;

    /**
     * 生成二维码
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //创建参数
        Map<String,String> param = new HashMap<>();
        //设置参数
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body","品优购，大秒杀");
        param.put("out_trade_no",out_trade_no);
        param.put("total_fee",total_fee);
        param.put("spbill_create_ip","192.168.25.128");
        param.put("notify_url","https://www.jd.com");
        param.put("trade_type","NATIVE");

        try {
            //生成发送请求的URl
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            //发送请求
            HttpClient httpClient = new HttpClient(url);
            //开启https协议
            httpClient.setHttps(true);
            //设置发送的XML参数
            String xmlParam = WXPayUtil.generateSignedXml(param, key);
            //System.out.println("请求的参数是："+xmlParam);
            httpClient.setXmlParam(xmlParam);
            //POST提交
            httpClient.post();
            //获取返回结果
            String result = httpClient.getContent();
            System.out.println(result);
            //xml ------ map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);

            Map<String,String> returnMap = new HashMap<>();
            returnMap.put("out_trade_no",out_trade_no);
            returnMap.put("total_fee",total_fee);
            returnMap.put("code_url",resultMap.get("code_url"));
            return returnMap;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("二维码请求失败");
            return null;
        }
    }


    //根据订单号查询支付状态
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map<String,String> param = new HashMap<>();
        //封装参数
        param.put("appid",appid);
        param.put("mch_id",partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("out_trade_no",out_trade_no);
        //目标请求路径
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param,key);
            //设置
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            //post提交
            client.post();
            //获取返回结果
            String result = client.getContent();
            Map<String, String> returnMap = WXPayUtil.xmlToMap(result);
            return returnMap;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //根据支付单号关闭支付接口
    @Override
    public Map closePay(String out_trade_no) {
        Map<String,String> param = new HashMap<>();
        //封装参数
        param.put("appid",appid);//公众号Id
        param.put("mch_id",partner);//商家Id
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("out_trade_no",out_trade_no);//订单号
        try {
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";
            //设置请求需要的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, key);
            //发送请求
            HttpClient client = new HttpClient(url);
            //开启识别https协议
            client.setHttps(true);
            //设置二维码需要的参数
            client.setXmlParam(xmlParam);
            //提交
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}