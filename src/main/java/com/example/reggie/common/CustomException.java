package com.example.reggie.common;


import org.springframework.web.bind.annotation.ExceptionHandler;

// 自定义异常
public class CustomException extends RuntimeException{
    public CustomException(String msg){
        super(msg);
    }
}
