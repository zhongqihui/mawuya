/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.listener;

import com.niudeyapi.mawuya.common.utils.CommonUtil;
import com.niudeyapi.mawuya.core.cache.DataCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Set;

/**
 * 自定义 session 监听器：session 销毁时累加阅读次数
 *
 * @author zqh
 */
public class MySessionListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(MySessionListener.class);

    /** session 默认 5 分钟过期 */
    private static final int DEFAULT_INACTIVE_INTERVAL_SECONDS = 60 * 5;

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        event.getSession().setMaxInactiveInterval(DEFAULT_INACTIVE_INTERVAL_SECONDS);
        if (log.isDebugEnabled()) {
            log.debug("session created, id={}, maxInactive={}s", event.getSession().getId(), DEFAULT_INACTIVE_INTERVAL_SECONDS);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String id = event.getSession().getId();
        HttpSession session = MySessionContext.getInstance().getSession(id);
        if (session != null) {
            @SuppressWarnings("unchecked")
            Set<String> aSnSet = (Set<String>) session.getAttribute(CommonUtil.ASNSET);
            if (aSnSet != null && !aSnSet.isEmpty()) {
                DataCenter.getReadNumToDBQueue().addAll(aSnSet);
            }
            MySessionContext.getInstance().delSession(session);
        }
        if (log.isDebugEnabled()) {
            log.debug("session destroyed, id={}", id);
        }
    }
}
