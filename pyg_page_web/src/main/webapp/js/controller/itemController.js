app.controller('itemController',function($scope,$http){

    //��������
    $scope.addNum = function (x) {
        $scope.num = $scope.num+x;
        if ($scope.num<1){
            $scope.num=1;
        }
    }
	//�ڷ����п�����������С��1
	
	 //��¼�û�ѡ��Ĺ��
    $scope.spec={};

    $scope.selectSpec=function (name, value) {
        //�û�ѡ����
        $scope.spec[name]=value;
		$scope.searchSku();
    }
    //�ж��û��Ƿ�ѡ��
    $scope.isSelected=function (name,value) {

        if ($scope.spec[name]==value){
            return true;
        }else {
            return false;
        }
    }
	
	 //����Ĭ�ϵ�sku
    $scope.loadSku = function(){
        //Ĭ�ϵ�sku,�󶨵�ҳ����
        $scope.sku = skuList[0];
        //ʹ����ȿ�¡
        $scope.specification = JSON.parse(JSON.stringify($scope.sku.spec));
    }
    //��ѯSKU
    $scope.searchSku=function () {
        //����skuList��ѭ������spec�͹���б���true��ִ�и���sku����
        for (var i = 0;i < skuList.length;i++){

            if (matchObject(skuList[i].spec,$scope.spec)){

               $scope.sku = skuList[i];
               return;
            }
        }
        //�������������
        $scope.sku={id:0,title:'-------',price:0};
    }
	  //ѡ�������SKU
    //ƥ����������
    matchObject=function (map1, map2) {

        for (var key in map1){//����map1����
            if (map1[key]!=map2[key]){
                return false;
            }
        }
        for (var key in map2){
            if (map2[key]!=map1[key]){
                //����map2����������ҵ�(������key��value)�������
                return false;
            }
        }
            return true;
    }
    //�����Ʒ�����ﳵ
    $scope.addGoodsToCartList = function(){
        // alert("�������Ʒid" + $scope.sku.id + "  ����" + $scope.num);
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='+$scope.sku.id+'&num='+ $scope.num,{'withCredentials':true}).success(
            function (response) {
                if(response.success){
                    //��ӳɹ�����ת�����ﳵҳ��
                    location.href="http://localhost:9107/";
                }else{
                    alert(response.message);
                }
            }
        );

    }
})



