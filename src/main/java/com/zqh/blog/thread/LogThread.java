package com.zqh.blog.thread;

import com.zqh.blog.cache.DataCenter;
import com.zqh.blog.entity.LogInfo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/1 15:23
 * description:
 **/
public class LogThread implements Runnable{

    private AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void run() {
        while (running.get()) {
            LogInfo logInfo = DataCenter.getLogInfoQueue().poll();
        }
    }

    public void shutDown() {
        running.set(false);
    }
}
