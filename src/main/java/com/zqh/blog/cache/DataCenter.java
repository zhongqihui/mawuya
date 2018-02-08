package com.zqh.blog.cache;

import com.zqh.blog.entity.LogInfo;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/2/7 21:20
* description: 缓存中心
**/
public class DataCenter {
    /*** 访客信息队列*/
    private static BlockingQueue<LogInfo> logInfoToAPIQueue = new ArrayBlockingQueue<LogInfo>(50000);

    /*** 访客信息入库队列*/
    private static BlockingQueue<LogInfo> logInfoToDBQueue = new ArrayBlockingQueue<LogInfo>(50000);

    private static BlockingQueue<String> readNumToDBQueue = new ArrayBlockingQueue<>(50000);


    public static BlockingQueue<LogInfo> getLogInfoToAPIQueue() {
        return logInfoToAPIQueue;
    }

    public static BlockingQueue<LogInfo> getLogInfoToDBQueue() {
        return logInfoToDBQueue;
    }

    public static BlockingQueue<String> getReadNumToDBQueue() {
        return readNumToDBQueue;
    }
}
