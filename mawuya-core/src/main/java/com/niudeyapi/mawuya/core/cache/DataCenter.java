/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.cache;

import com.niudeyapi.mawuya.core.dataobject.LogDO;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 缓存中心：保存异步处理的访客日志与阅读数累加队列
 *
 * @author zqh
 */
public class DataCenter {

    /** 访客信息：调用 IP 接口前的队列 */
    private static final BlockingQueue<LogDO> LOG_INFO_TO_API_QUEUE = new ArrayBlockingQueue<>(50000);

    /** 访客信息：等待入库的队列 */
    private static final BlockingQueue<LogDO> LOG_INFO_TO_DB_QUEUE = new ArrayBlockingQueue<>(50000);

    /** 文章 sn 队列：等待将 read_num + 1 入库 */
    private static final BlockingQueue<String> READ_NUM_TO_DB_QUEUE = new ArrayBlockingQueue<>(50000);

    private DataCenter() {
    }

    public static BlockingQueue<LogDO> getLogInfoToAPIQueue() {
        return LOG_INFO_TO_API_QUEUE;
    }

    public static BlockingQueue<LogDO> getLogInfoToDBQueue() {
        return LOG_INFO_TO_DB_QUEUE;
    }

    public static BlockingQueue<String> getReadNumToDBQueue() {
        return READ_NUM_TO_DB_QUEUE;
    }
}
