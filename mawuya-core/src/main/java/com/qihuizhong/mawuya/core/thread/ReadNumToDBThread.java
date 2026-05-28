/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.thread;

import com.qihuizhong.mawuya.core.cache.DataCenter;
import com.qihuizhong.mawuya.core.mapper.ArticleInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 文章阅读次数批量累加线程
 *
 * @author zqh
 */
@Component
@ConditionalOnProperty(prefix = "mawuya.async", name = "enabled", havingValue = "true")
public class ReadNumToDBThread implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ReadNumToDBThread.class);
    private static final int BATCH_LIMIT = 1000;

    @Autowired
    private ArticleInfoMapper articleInfoMapper;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public void shutDown() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            int size = DataCenter.getReadNumToDBQueue().size();
            try {
                if (size <= 0) {
                    Thread.sleep(3000L);
                    continue;
                }
                getSnList(Math.min(size, BATCH_LIMIT));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("ReadNumToDB update failed", e);
            }
        }
    }

    private void getSnList(int limit) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            String sn = DataCenter.getReadNumToDBQueue().poll();
            // 过滤非数字 sn，避免拼装到 SQL 中导致注入风险
            if (StringUtils.isNotEmpty(sn) && sn.matches("\\d+")) {
                sb.append(sn).append(",");
            }
        }

        String all = sb.toString();
        if (StringUtils.isNotEmpty(all) && all.endsWith(",")) {
            String sns = all.substring(0, all.length() - 1);
            long start = System.currentTimeMillis();
            int rows = articleInfoMapper.updateBatchReadNum(sns);
            if (log.isInfoEnabled()) {
                log.info("read_num batch +1: sns=[{}], rows={}, cost={}ms",
                        sns, rows, System.currentTimeMillis() - start);
            }
        }
    }
}
