/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.thread;

import com.qihuizhong.mawuya.core.cache.DataCenter;
import com.qihuizhong.mawuya.core.dataobject.LogDO;
import com.qihuizhong.mawuya.core.mapper.LogInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 访问信息批量入库线程
 *
 * @author zqh
 */
@Component
@ConditionalOnProperty(prefix = "mawuya.async", name = "enabled", havingValue = "true")
public class LogToDBThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LogToDBThread.class);
    private static final int BATCH_LIMIT = 1000;

    @Autowired
    private LogInfoMapper logInfoMapper;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public void shutDown() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            int count = DataCenter.getLogInfoToDBQueue().size();
            if (count <= 0) {
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            try {
                insert2DB(Math.min(count, BATCH_LIMIT));
            } catch (Exception e) {
                log.error("LogToDB insert failed", e);
            }
        }
    }

    private void insert2DB(int limit) {
        List<LogDO> infos = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            LogDO info = DataCenter.getLogInfoToDBQueue().poll();
            if (info != null) {
                infos.add(info);
            }
        }
        if (infos.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        int rows = logInfoMapper.insertBatch(infos);
        if (log.isInfoEnabled()) {
            log.info("logInfo batch insert: total={}, rows={}, cost={}ms",
                    infos.size(), rows, System.currentTimeMillis() - start);
        }
    }
}
