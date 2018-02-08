package com.zqh.blog.listener;

import com.zqh.blog.cache.DataCenter;
import com.zqh.blog.utils.CommonUtil;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Set;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/2/7 22:18
* description: 自定义session监听器
**/
public class MySessionListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        //设置session的失效时间，单位s
        event.getSession().setMaxInactiveInterval(60);
        System.out.println("设置session的失效时间为60，单位s");
    }

    /**
     * Session 失效时，将该session访问过的文章readNum + 1，放入集合中，等待线程将其入库
     * @param event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String id = event.getSession().getId();
        HttpSession session = MySessionContext.getInstance().getSession(id);
        if(session != null) {
            Set<String> aSnSet = (Set<String>) session.getAttribute(CommonUtil.ASNSET);
            if(aSnSet.size() > 0) {
                DataCenter.getReadNumToDBQueue().addAll(aSnSet);
            }

            MySessionContext.getInstance().delSession(session);
        }

        System.out.println("session:" + id + "销毁！！！！！！！！！！！！阅读次数 + 1");
    }
}
