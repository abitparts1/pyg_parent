app.controller('baseController',function ($scope) {

    //定义分页工具条参数
    $scope.paginationConf={
        currentPage: 1,
        totalItems: 0,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    }

    //刷新页面
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds=[];
    //当勾选复选框添加id元素，取消勾选，移除id元素
    $scope.updateSelection=function($event,id){
        if($event.target.checked){//true 勾选
            $scope.selectIds.push(id);
        }else{//取消勾选，移除元素
            var index=$scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    }
    //把json字符串转化成json对象，再用String对象接受再用“，”分割
    $scope.jsonToString=function (jsonString,key) {
        var json = jsonString.parse(jsonString)
        var value = ""
        for (var i =0;i<jsonString.length;i++){
            if (i>0){
                value+=",";
            }
            value+=json[i][key];
        }
        return value;
    }
    //当复选框勾选添加id元素，没勾选移除id元素
    $scope.updateSelection=function ($event, id) {
        if ($event.target.checked){
            $scope.selectIds.push(id)
        }else {
            var index = $scope.selectIds.lastIndexOf(index);
            $scope.selectIds.splice(index,1);
        }
    }
    //把response转成json格式，并通过key对应向获得的值给取出来
    $scope.jsonToString=function (jsonString, key) {
        var value = "";//定义一个空串以拼接对象值
        var json = JSON.parse(jsonString);//字符串转成json
        for (var i = 0;i<json.length;i++){
            if (i>0){
                value+=",";//遍历并在对象值后面加逗号
            }
            value+=json[i][key];
        }
        return value;
    }
})