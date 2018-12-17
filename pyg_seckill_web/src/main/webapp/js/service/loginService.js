app.service('loginService',function ($http) {

    this.getLoginUser=function () {
        return $http.get('../getLoginUser.do');
    }
})