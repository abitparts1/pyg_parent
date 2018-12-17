app.controller('cartController',function ($scope, cartService) {


    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList=response;
                //总金额和总数量的计算
                $scope.total = {'totalNum':0,'totalMoney':0.0};

                for(var i=0;i<$scope.cartList.length;i++){
                    var cart =   $scope.cartList[i];
                    for(var j=0;j<cart.orderItemList.length;j++){
                        $scope.total.totalNum += cart.orderItemList[j].num;
                        $scope.total.totalMoney += cart.orderItemList[j].totalFee;
                    }
                }
            }
        );
    }
    //添加商品到购物车
    $scope.addGoodsToCartList=function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
              if (response.success){
                  $scope.findCartList();
                  alert(response.message);
              }else {
                  alert(response.message);
              }
            }
        )
    }
    //获取地址栏列表
    $scope.findAddressList=function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList=response;
               /// alert(JSON.stringify(response));
                //设置默认地址
                for (var i = 0;i < $scope.addressList.length;i++){
                    if ($scope.addressList[i].isDefault=='1'){
                        $scope.selectedAddress=$scope.addressList[i]
                        break;
                    }
                }
            }
        )
    }
    //选择地址
    $scope.selectAddress=function (address) {
        //alert("selectAddress");
        $scope.selectedAddress=address;
    }
    //判断是否是当前选中的地址
    $scope.isSelected=function (address) {
      //  alert("isSelected");
        if ($scope.selectedAddress==address){
            return true;
        }
        return false;
    }
    //结算方式,传递信息的对象：地址和支付方式(变量)
    $scope.order={'paymentType':'1'}
    //改变结算的方式
    $scope.changeType=function (type) {
        $scope.order.paymentType=type;
    }
    //提交订单
    $scope.submitOrder=function () {
        //处理收件人地址
        $scope.order.receiver=$scope.selectedAddress.contact;
        $scope.order.receiverMobile=$scope.selectedAddress.mobile;
        $scope.order.receiverAreaName=$scope.selectedAddress.address;
        //提交成功判断是否时微信支付，若是就跳转支付页面
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success){
                    //开始页面跳转
                    if ($scope.order.paymentType=='1'){
                        //在线支付，跳转到支付页面
                        location.href="pay.html";
                    }else {
                        //货到付款跳转到支付成功页面
                        location.href="paysuccess.html";
                    }
                }else {
                    alert(response.message)
                }
            }
        )


    }
})