package com.pyg.pay.service;

import java.util.Map;

public interface WeChatPayService {

    //微信支付,根据商户订单号和支付金额生成订单
    public Map createNative(String out_trade_no,String total_fee);
    /**
     * 查询支付状态
     * @param out_trade_no
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     *   根据交易单号关闭支付
     * @param out_trade_no
     * @return
     */
    public Map closePay(String out_trade_no);
}
