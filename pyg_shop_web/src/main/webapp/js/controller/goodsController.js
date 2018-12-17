 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

    $scope.status=['未审核','审核通过','审核未通过','已关闭'];
    //显示分类名称
    //1.定义一个分类数组
    $scope.itemCatList = [];
    //2.加载商品分类列表
    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0;i<response.length;i++){
                    $scope.itemCatList[response[i].id]=response[i].name;
                }
            }
        )
    }
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
		var id = $location.search()['id'];
		if (id!=undefined){
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
                editor.html($scope.entity.goodsDesc.introduction);///错的
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems)
				for (var i = 0;i<$scope.entity.itemList.length;i++){
					 $scope.entity.itemList.spec[i] =JSON.parse($scope.entity.itemList.spec[i])
				}
			}
		);
		}
	}
	//根据于规格和名称返回是否被勾选
	$scope.isChecked=function (name, value) {
        var object = searchObjectByKey( $scope.entity.goodsDesc.specificationItems,name,'attributeName');//错的
		if (object!=null){
			if (object.attributeValue.indexOf(value)>=0){
				return true;
			}else {
				return false;
			}
		}else{
			return false;
		}
    }
	
	//保存
	$scope.save=function(){
        $scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加
		}				
		serviceObject.success(
            function(response){
                if(response.success){
                    //建议页面跳转
                    $scope.entity={goods:{},goodsDesc:{itemImages:[]},itemList:[]};
                    editor.html('');
                }else{
                    alert(response.message);
                }
            }
        );
	}
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	//上传图片
	$scope.uploadFile=function () {
	uploadService.uploadFile().success(
		function (response) {
		if (response.success) {
            $scope.image_entity.url = response.message//设置文件地址
			document.getElementById("file").value="";
        }else {
			alert(response.message)
		}
    })
    }
    //添加图片
	//定义页面图片实体
	$scope.add_image_entity=function () {
		//因为照片内容较长，所以上传到扩展表
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity)
    }
    //列表中移除图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1)
    }
    //读取一级分类列表
	$scope.findItemCat1List=function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List=response;
        })
    }
    //读取二级下拉选项:根据选择的id值entity.goods.category1Id来作为父id进行查询
	//设置新旧值，如果取消已选中的id就清空显示信息的集合，这里使用监测事件
	$scope.$watch("entity.goods.category1Id",function (newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat2List=response;
            }
		)
    })
	//读取三级下拉选
	$scope.$watch("entity.goods.category2Id",function (newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat3List=response;
            }
		)
    })
	//读取模板Id，根据第三级ID查询出实体（findOne）再更新模板id
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId=response.typeId;
            }
		)
    })
	//选择模板Id后更行品牌列表，因为模板对象中含有品牌信息的json字符串，所以问哦们通过模板对象获得品牌Id
	//并展示在下拉框
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {

        typeTemplateService.findOne(newValue).success(
			function (response) {
				$scope.typeTemplate=response;//获得模板类型列表
                $scope.brandIds=JSON.parse(response.brandIds);
            }
		)
        //获取规格数据
        typeTemplateService.findSpecListByTypeId(newValue).success(
        	function (response) {
				$scope.specList=response;
            }
		)

    })

    $scope.updateSpecAttribute=function ($event,name,value) {
        var specItems=$scope.entity.goodsDesc.specificationItems;//勾选结果
        var object = $scope.searchObjectByKey(specItems,name,'attributeName');
        if(object!=null){//该规格被勾选过 添加元素
            if($event.target.checked){
                object.attributeValue.push(value);
            }else{//取消勾选，移除元素
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                if(object.attributeValue.length==0){//上面移除的是最后一个元素，要移除整个对象
                    specItems.splice(specItems.indexOf(object),1);
                }
            }

        }else{//没被勾选过，添加对象
            specItems.push({"attributeName":name,"attributeValue":[value]});
        }

    }
    //判断该规格在勾选结果中是否存在  1list勾选结果  2name规格名称
    $scope.searchObjectByKey=function (list, name, key) {
        //遍历对比key是否等于name
        for (var i = 0;i<list.length;i++){
            if (list[i][key]==name){
                return list[i];
            }
        }
        return null;
    }
    $scope.entity={goods:{isEnableSpec:'1'},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};//组合实体类  这个对象只定义一次

    //生产SKU列表
    $scope.createItemList=function(){
        var specItems=$scope.entity.goodsDesc.specificationItems;//勾选结果
        $scope.entity.itemList=[{spec:{},price:0,num:9999,status:'1',isDefault:'0'}];
        //勾选结果格式
        /*[{"attributeName":"容量","attributeValue":["1L",2L","3L"]},
                {"attributeName":"香型","attributeValue":["浓香","酱香","清香"]}，] */
        //生成数据
        for(var i=0;i<specItems.length;i++){
            $scope.entity.itemList=addColumn($scope.entity.itemList,specItems[i].attributeName,specItems[i].attributeValue);
        }
    }
    //给列对象添加值
	addColumn=function (list,name,values) {
		var newList = [];
		for (var i=0;i<list.length;i++){
			var oldRow=list[i]
			for (var j = 0;j<values.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[name]=values[j]
				newList.push(newRow)
			}
		}
		return newList;
    }
});
