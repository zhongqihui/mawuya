package com.zqh.blog.cache;

import com.zqh.blog.entity.LogInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 钟启辉
 * @description
 * @company www.jiweitech.com
 * @date 2018-02-01 10:57
 **/
public class DataCenter {
    private static BlockingQueue<LogInfo> logInfoToAPIQueue = new ArrayBlockingQueue<LogInfo>(50000);
    private static BlockingQueue<LogInfo> logInfoToDBQueue = new ArrayBlockingQueue<LogInfo>(50000);
    private static Map<String, Integer> articleReadNum = new ConcurrentHashMap<>();

    public static BlockingQueue<LogInfo> getLogInfoToAPIQueue() {
        return logInfoToAPIQueue;
    }

    public static BlockingQueue<LogInfo> getLogInfoToDBQueue() {
        return logInfoToDBQueue;
    }

    public static Map<String, Integer> getArticleReadNum() {
        return articleReadNum;
    }
}
