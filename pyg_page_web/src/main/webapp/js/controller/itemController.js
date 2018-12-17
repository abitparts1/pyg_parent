app.controller('itemController',function($scope,$http){

    //数量操作
    $scope.addNum = function (x) {
        $scope.num = $scope.num+x;
        if ($scope.num<1){
            $scope.num=1;
        }
    }
	//在方法中控制数量不能小于1
	
	 //记录用户选择的规格
    $scope.spec={};

    $scope.selectSpec=function (name, value) {
        //用户选择规格
        $scope.spec[name]=value;
		$scope.searchSku();
    }
    //判断用户是否被选中
    $scope.isSelected=function (name,value) {

        if ($scope.spec[name]==value){
            return true;
        }else {
            return false;
        }
    }
	
	 //加载默认的sku
    $scope.loadSku = function(){
        //默认的sku,绑定到页面上
        $scope.sku = skuList[0];
        //使用深度克隆
        $scope.specification = JSON.parse(JSON.stringify($scope.sku.spec));
    }
    //查询SKU
    $scope.searchSku=function () {
        //遍历skuList，循环传入spec和规格列表返回true就执行更新sku变量
        for (var i = 0;i < skuList.length;i++){

            if (matchObject(skuList[i].spec,$scope.spec)){

               $scope.sku = skuList[i];
               return;
            }
        }
        //如果不满足条件
        $scope.sku={id:0,title:'-------',price:0};
    }
	  //选择规格更新SKU
    //匹配两个对象
    matchObject=function (map1, map2) {

        for (var key in map1){//根据map1查找
            if (map1[key]!=map2[key]){
                return false;
            }
        }
        for (var key in map2){
            if (map2[key]!=map1[key]){
                //根据map2找如果都能找到(数量，key和value)，就相等
                return false;
            }
        }
            return true;
    }
    //添加商品到购物车
    $scope.addGoodsToCartList = function(){
        // alert("购买的商品id" + $scope.sku.id + "  数量" + $scope.num);
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+ $scope.num,{'withCredentials':true}).success(
            function (response) {
                if(response.success){
                    //添加成功，跳转到购物车页面
                    location.href="http://localhost:9107/";
                }else{
                    alert(response.message);
                }
            }
        );

    }
})



