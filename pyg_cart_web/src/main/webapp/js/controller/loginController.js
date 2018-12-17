app.controller('loginController',function ($scope, loginService) {

    //获取用户当前登陆名
    $scope.getLoginUser=function () {
        loginService.getLoginUser().success(
            function (response) {
                $scope.username=response.username;
            }
        )
    }
})