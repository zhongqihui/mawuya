package com.zqh.blog.thread;

import com.zqh.blog.cache.DataCenter;
import com.zqh.blog.entity.LogInfo;
import com.zqh.blog.utils.OKHttpUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/1 15:23
 * description:
 * 获取队列中的用户访问信息，获取信息的ip，
 * 调用免费的ip接口，获取ip的地理位置
 **/
@Component
public class LogToAPIThread implements Runnable {

    private AtomicBoolean running = new AtomicBoolean(true);

    public void shutDown() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            if (!DataCenter.getLogInfoToAPIQueue().isEmpty()) {
                LogInfo logInfo = DataCenter.getLogInfoToAPIQueue().poll();
                OKHttpUtil.taobaoSend(logInfo);
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
