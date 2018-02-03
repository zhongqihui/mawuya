package com.zqh.blog.cache;

import com.zqh.blog.entity.LogInfo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author 钟启辉
 * @description
 * @company www.jiweitech.com
 * @date 2018-02-01 10:57
 **/
public class DataCenter {
    private static BlockingQueue<LogInfo> logInfoQueue = new ArrayBlockingQueue<LogInfo>(50000);

    public static BlockingQueue<LogInfo> getLogInfoQueue() {
        return logInfoQueue;
    }
}
