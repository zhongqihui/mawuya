package com.zqh.blog.listener;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/2/7 22:26
* description: 单例自定义session上下文
**/
public class MySessionContext {
    private static MySessionContext mySessionContext;
    private Map<String, HttpSession> sessionMap;
    private Set<String> aSnSet = new HashSet<>();

    private MySessionContext() {
        sessionMap = new ConcurrentHashMap<>();
    }

    public static MySessionContext getInstance() {
        if(mySessionContext == null) {
            mySessionContext = new MySessionContext();
        }

        return mySessionContext;
    }

    public void addSession(HttpSession session) {
        sessionMap.put(session.getId(), session);
    }

    public void delSession(HttpSession session ) {
        sessionMap.remove(session.getId());
    }

    public HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     *  将某个session阅读的文章sn，存放在session的"aSnSet"中，当该session销毁时，将这些sn的文章readNum + 1
     * @param session
     * @param sn
     */
    public void addArticleSn2Session(HttpSession session, String sn) {
        if(session == null) {
            return;
        }

        HttpSession s = getSession(session.getId());
        if(s == null) {
            s = session;
            addSession(s);
        }

        Set<String> aSnSet = (Set<String>) s.getAttribute("aSnSet");
        if(aSnSet == null) {
            aSnSet = new HashSet<>();
            session.setAttribute("aSnSet", aSnSet);
        }

        aSnSet.add(sn);
    }
}
