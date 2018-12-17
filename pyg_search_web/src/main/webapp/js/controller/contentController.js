 //控制层 
app.controller('contentController' ,function($scope,contentService){

	$scope.contentList=[];

	//获取某一类型的广告列表
	$scope.findListByCategoryId=function (categoryId) {
        contentService.findListByCategoryId(categoryId).success(
        	function (response) {
                $scope.contentList[categoryId]=response;
            }
		)
    }
});	
