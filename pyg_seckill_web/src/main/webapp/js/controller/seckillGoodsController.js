app.controller('seckillGoodsController',function ($scope,$location,$interval, seckillGoodsService) {

    //查询当前参与秒杀的商品
    $scope.findList=function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list=response;
            }
        )
    }
    //根据Id查询redis中的商品详情页信息
    $scope.findOne=function () {
        seckillGoodsService.findOne($location.search()['id']).success(
            function (response) {
                $scope.entity=response;
                //总秒数
                var allsecond=Math.floor((new Date($scope.entity.endTime).getTime() - (new Date().getTime()))/1000)

                var time=$interval(function () {
                    if (allsecond>0){
                        allsecond=allsecond-1;
                        //转换成时间字符串
                        $scope.time_title=convertTime(allsecond);
                    }else {
                        $interval.cancel(time);
                        alert("秒杀结束")
                    }
                },1000)
            }
        )

        convertTime = function (allSecond) {
            // 剩余天数  =  Math.floor(剩余秒杀 / 24 * 60 * 60)

            // 剩余小时  = Math.floor((剩余秒数 - 剩余天数 * 24 * 60 * 60 ) /  60 * 60)

            // 剩余分钟 = Math.floor(剩余秒数  - 剩余天数 * 24 * 60 * 60 - 剩余小时 * 60 * 60 ) / 60

            // 剩余秒数 = 剩余秒数  - 剩余天数 * 24 * 60 * 60 - 剩余小时 * 60 * 60 - 剩余分钟 * 60

            //  $scope.time_title = 剩余天数+ 剩余小时 + 剩余分钟 + 剩余秒数

            var days= Math.floor( allSecond/(60*60*24));//天数
            var hours= Math.floor( (allSecond-days*60*60*24)/(60*60) );//小时数
            var minutes= Math.floor(  (allSecond -days*60*60*24 - hours*60*60)/60    );//分钟数
            var seconds= allSecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
            var timeString="";
            if(days>0){
                timeString=days+"天 ";
            }
            return timeString+hours+":"+minutes+":"+seconds;

        }
    }
    //根据商品Id提交订单
    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {
                alert($scope.entity.id);
                if (response.success){
                    alert("下单成功，跳转到支付页面");
                    location.href="pay.html";
                }else {
                    alert(response.message);
                }
            }
        )
    }
})