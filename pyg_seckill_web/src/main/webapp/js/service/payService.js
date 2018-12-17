app.service('payService',function ($http) {
    //获取预支付url
    this.createNative = function () {
        return $http.get('../pay/createNative.do');
    }

    //查询订单的支付状态
    this.queryPayStatus = function (out_trade_no) {
        return $http.get('../pay/queryPayStatus.do?out_trade_no='+out_trade_no);
    }
})