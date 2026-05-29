/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.listener;

import com.qihuizhong.mawuya.common.utils.CommonUtil;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义 session 上下文，单例。
 * <p>使用静态内部类持有，线程安全。</p>
 *
 * @author zqh
 */
public class MySessionContext {

    private final Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();

    private MySessionContext() {
    }

    private static final class Holder {
        private static final MySessionContext INSTANCE = new MySessionContext();
    }

    public static MySessionContext getInstance() {
        return Holder.INSTANCE;
    }

    public void addSession(HttpSession session) {
        sessionMap.put(session.getId(), session);
    }

    public void delSession(HttpSession session) {
        sessionMap.remove(session.getId());
    }

    public HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * 将访问的文章 sn 记入当前 session，session 销毁时统一回流到阅读数队列。
     */
    public void addArticleSn2Session(HttpSession session, String sn) {
        if (session == null) {
            return;
        }

        HttpSession s = getSession(session.getId());
        if (s == null) {
            s = session;
            addSession(s);
        }

        @SuppressWarnings("unchecked")
        Set<String> aSnSet = (Set<String>) s.getAttribute(CommonUtil.ASNSET);
        if (aSnSet == null) {
            // 单 session 阅读文章数通常 < 8，初始容量给 8 足够
            aSnSet = new HashSet<>(8);
            session.setAttribute(CommonUtil.ASNSET, aSnSet);
        }

        aSnSet.add(sn);
    }
}
