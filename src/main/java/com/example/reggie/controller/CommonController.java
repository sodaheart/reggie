package com.example.reggie.controller;

import com.example.reggie.common.CustomException;
import com.example.reggie.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

// 图片上传和下载管理
@RestController
@RequestMapping("/common")
public class CommonController {
    private final String path=System.getProperty("user.dir") + "/upload/";

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        String originalFilename = file.getOriginalFilename();// 得到文件名字:xxx.jpg
        if(originalFilename==null){
            throw new CustomException("服务器未得到名字");
        }// 判断是否得到名字是否成功
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//得到文件后缀
        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID() + suffix; // uuid+".jpg"
        //创建一个目录对象
        File dir = new File(path);
        //判断当前目录是否存在
        if(!dir.exists()){
            //目录不存在，需要创建
            boolean ans = dir.mkdirs();
            if(!ans){
                throw new CustomException("目标文件夹创建失败");
            }
        }
        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(path + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 回显图片名字
        return R.success(fileName);
    }

    @GetMapping("/download")
    public void down(String name, HttpServletResponse resp){
        try(FileInputStream fileInputStream = new FileInputStream(path + name);
            ServletOutputStream outputStream = resp.getOutputStream()) {
            resp.setContentType("image/jpeg");
            int len;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
