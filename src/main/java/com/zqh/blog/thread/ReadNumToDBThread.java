package com.zqh.blog.thread;

import com.zqh.blog.cache.DataCenter;
import com.zqh.blog.mapper.ArticleInfoMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/7 19:02
 * description: 文章阅读次数入库线程
 **/
@Component
public class ReadNumToDBThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ReadNumToDBThread.class);

    @Autowired
    private ArticleInfoMapper articleInfoMapper;

    private AtomicBoolean running = new AtomicBoolean(true);

    public void shutDown() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            int numQueueSize = DataCenter.getReadNumToDBQueue().size();
            try {
                if (numQueueSize <= 0) {
                    Thread.sleep(3000L);
                    continue;
                }

                if (numQueueSize < 1000) {
                    getSnList(numQueueSize);
                } else {
                    getSnList(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 批量修改文章的阅读次数
     * @param limit
     */
    private void getSnList(int limit) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < limit; i++) {
            String sn = DataCenter.getReadNumToDBQueue().poll();
            if (StringUtils.isNotEmpty(sn)) {
                sb.append(sn).append(",");
            }
        }

        if (StringUtils.isNotEmpty(sb.toString()) && sb.toString().endsWith(",")) {
            String sns = sb.toString().substring(0, sb.toString().length() - 1);
            long l = System.currentTimeMillis();
            int i = articleInfoMapper.updateBatchReadNum(sns);
            if (log.isInfoEnabled()) {
                log.info("批量修改sns：" + sns + "的阅读次数 + 1 ;成功修改记录数：" + i + ";耗时：" + (System.currentTimeMillis() - l) + "ms");
            }
        }
    }
}
