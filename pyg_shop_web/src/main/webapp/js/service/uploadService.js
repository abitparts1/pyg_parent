//文件上传服务层
app.service("uploadService",function ($http) {
    this.uploadFile=function () {
        //文件上传需要表单，所以这里new一个form类
        var formData = new FormData();
        //表单添加选项
        formData.append("file",file.files[0])
        return $http({
            method:'POST',
            url:'../uploadFile.do',
            data:formData,
            headers: {'Content-Type':undefined},
            transformRequest: angular.identity
            })
    }
})