app.controller("indexController",function ($scope, loginService) {
    //获取当前登陆人
    $scope.showLoginName=function () {
        loginService.loginName().success(
            function (responce) {
                $scope.loginName=responce.loginName;
    })
    }
})