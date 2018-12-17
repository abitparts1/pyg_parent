 //控制层 
app.controller('contentController' ,function($scope,$location,contentService){

	$scope.contentList=[];

	//获取某一类型的广告列表
	$scope.findListByCategoryId=function (categoryId) {
        contentService.findListByCategoryId(categoryId).success(
        	function (response) {
                $scope.contentList[categoryId]=response;
            }
		)
    }
    //跳转搜索页
    $scope.search =function () {
        location.href = "http://localhost:9104/search.html#?keywords=" +$scope.keywords;
    }
});	
