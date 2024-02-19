package com.example.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp= (HttpServletResponse) servletResponse;
        //获取本次请求的url
        String reqURI=req.getRequestURI();
        //放过路径
        String[] passUris=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login"
        };
        //是否进行处理
        if(check(passUris,reqURI)){
            filterChain.doFilter(req,resp);
            return;
        }
        if(req.getSession().getAttribute("employee")!=null){
            BaseContext.setCurrentId((Long) req.getSession().getAttribute("employee"));
            filterChain.doFilter(req,resp);
            return;
        }
        if(req.getSession().getAttribute("user")!=null){
            BaseContext.setCurrentId((Long) req.getSession().getAttribute("user"));
            filterChain.doFilter(req,resp);
            return;
        }
        resp.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }
    private boolean check(String[] uris,String uri){
        for(String u:uris){
            boolean match = PATH_MATCHER.match(u,uri);
            if(match){
                return true;
            }
        }
        return false;
    }
}
