package com.zqh.blog.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/2/7 22:18
* description: 自定义session监听器
**/
public class MySessionListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        //MySessionContext.getInstance().addSession(event.getSession());
        //单位s
        event.getSession().setMaxInactiveInterval(60);
        System.out.println("添加session");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        //todo session 一销毁，则将该session中的读的文章次数 + 1
        System.out.println(event.getSession().getId() + ": session 一销毁，则将该session中的读的文章次数 + 1");
    }
}
