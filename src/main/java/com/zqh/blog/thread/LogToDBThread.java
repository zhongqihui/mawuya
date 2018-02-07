package com.zqh.blog.thread;

import com.zqh.blog.cache.DataCenter;
import com.zqh.blog.entity.LogInfo;
import com.zqh.blog.mapper.LogInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/7 14:33
 * description: 访问信息入库线程
 **/
@Component
public class LogToDBThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LogToDBThread.class);

    @Autowired
    private LogInfoMapper logInfoMapper;

    private AtomicBoolean running = new AtomicBoolean(true);

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
                    e.printStackTrace();
                }

                continue;
            }

            try {
                if (count < 1000) {
                    insert2DB(count);
                } else {
                    insert2DB(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 批量插入到数据库中
     *
     * @param limit
     * @return
     * @throws Exception
     */
    private void insert2DB(int limit) throws Exception {
        List<LogInfo> infos = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            LogInfo info = DataCenter.getLogInfoToDBQueue().poll();
            if (info != null) {
                infos.add(info);
            }
        }

        long l = System.currentTimeMillis();
        int insert = logInfoMapper.insertBatch(infos);
        if (log.isInfoEnabled()) {
            log.info("访客批量插入条数：" + infos.size() + ";实际入库条数："
                    + insert + ";耗时：" + (System.currentTimeMillis() - l) + "ms");
        }
    }


}
