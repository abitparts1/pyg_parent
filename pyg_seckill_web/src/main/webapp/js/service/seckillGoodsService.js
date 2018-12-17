app.service('seckillGoodsService', function ($http) {

    //查询当前参与秒杀的商品
    this.findList=function () {
        return $http.get("../seckillGoods/findList.do")
    }
    //根据id查询redis中的商品详情页信息
    this.findOne=function (id) {
        return $http.get("../seckillGoods/findOneFromRedis.do?id="+id);
    }
    this.submitOrder=function (seckillId) {
        return $http.get("../seckillOrder/submitOrder.do?seckillId="+seckillId);
    }
})