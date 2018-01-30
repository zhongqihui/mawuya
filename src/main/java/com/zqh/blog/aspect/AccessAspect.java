package com.zqh.blog.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/30 15:33
 * description:
 **/
@Aspect
@Component
public class AccessAspect {

    @Pointcut("execution (* com.zqh.blog.controller..*(..))")
    public void anyMethod(){}

    @Before("anyMethod()")
    public void printLog() {
        System.out.println("hello...");
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();//获取request
        StringBuffer requestURL = request.getRequestURL();
        System.out.println(requestURL);
    }
}
