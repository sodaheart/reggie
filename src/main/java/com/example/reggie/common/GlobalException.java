package com.example.reggie.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;
// 全局异常处理
@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
public class GlobalException {
    // 用于处理向表中插入已经存在但是不能重复的数据
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException e){
        log.info(e.getMessage());
        if(e.getMessage().contains("Duplicate entry")){
            String[] s=e.getMessage().split(" ");
            String msg = s[2] + "已经存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }
    // 异常提示
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException e){
        log.info(e.getMessage());
        return R.error(e.getMessage());
    }
}
