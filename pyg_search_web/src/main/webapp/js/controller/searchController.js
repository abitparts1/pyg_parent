 //控制层 
app.controller('searchController' ,function($scope,$location,searchService){

    $scope.searchMap = {'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':20,'sortField':'','sort':''};

    //页面初始化，执行搜索
    $scope.loadSearch=function () {
        var keywords = $location.search()['keywords'];
        if (keywords!=null){
            $scope.searchMap.keywords = keywords;
            $scope.search();
        }
    }

    $scope.resultMap={totalPages:''};
	//搜索
	$scope.search=function () {
	    $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
        	function (response) {
        	  //  alert(JSON.stringify(response));
                $scope.resultMap = response;
                $scope.buildPageLabel();//调用
            }
		)
    }
    //添加搜索选项的方法
    $scope.addSearchItem = function (key,value) {

        if(key == 'category' || key == 'brand' || key=='price' ){
            $scope.searchMap[key] = value;
        }else{
            $scope.searchMap.spec[key] = value;
            alert(key)
        }
        //触发重新搜索
        $scope.search();

    }
    $scope.removeSearchItem=function (key) {
        if (key=='category'|| key=='brand' || key=='price'){
            $scope.searchMap[key]='';
        }else {
            //是规格的话移除属性
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }
    //分页
    $scope.buildPageLabel=function () {
        $scope.firstDot = false;//前面无点
        $scope.lastDot = false;//后面无点
        $scope.pageLabel=[];//新增分页栏属性
        var maxPageNo = $scope.resultMap.totalPages;//得到最后页码
        var firstPage = 1; //开始页码
        var lastPage = maxPageNo;//截止页码
        if ($scope.resultMap.totalPages > 5) {//如果总页码大于5，则显示部分页码（5页）
            if ($scope.searchMap.pageNo <= 3) {//如果当前页小于等于3，显示5页
                lastPage = 5;
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {
                //如果当前页大于最后页码减2，显示后五页
                firstPage = maxPageNo - 4;
            } else {   //显示当前页为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        }
            for (var i = firstPage; i <= lastPage; i++) {
                $scope.pageLabel.push(i);
            }

    }
    //根据页码执行查询
    $scope.queryByPage=function (pageNo) {
        //页码验证,如果当前页码小于1，或大于最大页码就返回此函数
        if (pageNo<1||pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }
    //判断当前页为第一页
    $scope.isTopPage=function () {
        if ($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }
    //判断当前页是否是最后一页
    $scope.isEndPage=function () {
        if ($scope.resultMap.totalPages!=null && $scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }
    //排序的方法
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }
    //判断关键字是不是品牌
   $scope.keywordsIsBrand=function () {
       var brandList = $scope.resultMap.brandList;
       for (var i = 0;i<brandList.length;i++){
           if ($scope.searchMap.keywords.indexOf(brandList[i].text)>=0){
               return true;
           }
       }
       return false;
   }
});	
