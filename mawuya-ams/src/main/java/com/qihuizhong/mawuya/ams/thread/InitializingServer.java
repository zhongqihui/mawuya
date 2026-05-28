/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.thread;

import com.qihuizhong.mawuya.core.thread.LogToAPIThread;
import com.qihuizhong.mawuya.core.thread.LogToDBThread;
import com.qihuizhong.mawuya.core.thread.ReadNumToDBThread;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 前台启动后初始化：拉起 3 个守护线程。
 * <p>仅在 ams 模块装载，bms 不引入此类。同时叠加 {@code mawuya.async.enabled=true}
 * 与三个 Runnable 的条件装配保持一致。</p>
 *
 * @author zqh
 */
@Component
@ConditionalOnProperty(prefix = "mawuya.async", name = "enabled", havingValue = "true")
public class InitializingServer implements InitializingBean {

    @Autowired
    private LogToAPIThread logToAPIThread;
    @Autowired
    private LogToDBThread logToDBThread;
    @Autowired
    private ReadNumToDBThread readNumToDBThread;

    @Override
    public void afterPropertiesSet() {
        Thread log2API = new Thread(logToAPIThread, "LogToAPIThread");
        log2API.setDaemon(true);
        log2API.start();

        Thread log2DB = new Thread(logToDBThread, "LogToDBThread");
        log2DB.setDaemon(true);
        log2DB.start();

        Thread readNum2DB = new Thread(readNumToDBThread, "ReadNumToDBThread");
        readNum2DB.setDaemon(true);
        readNum2DB.start();
    }
}
