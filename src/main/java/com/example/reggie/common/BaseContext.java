package com.example.reggie.common;


// 每一个请求都有一个固定的线程id，我们用这个线程id来绑定用户的id
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    // 设置用户id
    public static  void setCurrentId(Long id){
        threadLocal.set(id);
    }
    // 获得用户id
    public static Long getCurrentId(){
        return threadLocal.get();
    }

    //成员变量：类型+名称;
    //成员方法：返回值+名称（）{}

}
