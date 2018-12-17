//服务层
app.service('searchService',function($http){
	    	
	//搜索获取结果
	this.search=function(searchMap){
		return $http.post('search/searchItem.do',searchMap);
	}
});
