app.controller('payController',function ($scope,$location, payService) {

    //页面初始化，生成二维码
    $scope.createNative = function () {

        payService.createNative().success(
            function (response) {
                //订单号和总金额显示
                $scope.out_trade_no = response.out_trade_no;
                //转换成元
                $scope.total_fee = (response.total_fee / 100).toFixed(2);

                //生成二维码
                var qr = window.qr = new QRious({
                    element: document.getElementById('qrious'),
                    size: 250,
                    level:'H',
                    value: response.code_url
                });

                queryPayStatus(response.out_trade_no);
            }
        );
    }

    //查询订单的支付状态
    queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(
            function (response) {
                if(response.success){
                    //支付成功，跳转到支付成功页面
                    location.href = "paysuccess.html#?money="+$scope.total_fee;

                }else{
                    //支付失败，跳转到失败页面
                    if(response.message == 'PAY_TIME_OUT'){
                        alert("二维码支付超时，点击确定重新生成！");
                        $scope.createNative();
                    }else{
                        location.href = "payfail.html";
                    }
                }
            }
        );
    }
    //获取金额
    $scope.getMoney=function () {
        $scope.money=$location.search()['money'];
    }
})