package com.pyg.manger.controller;

import com.pyg.FastDFSClient;
import com.pyg.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    //文件服务器地址
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @RequestMapping("uploadFile")//参数必须设置多个文件的
    public Result uploadFile(MultipartFile file){
        try {
            //取文件的原名称
            String originalFilename = file.getOriginalFilename();
            //因为文件原名有“.”所以把要把点去掉，取扩展名后缀
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //创建一个fastFDS客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //执行上传
            String path = fastDFSClient.uploadFile(file.getBytes(), extName,null);
            System.out.println(path);
            return new Result(true,FILE_SERVER_URL+path);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
