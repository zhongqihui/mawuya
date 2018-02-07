package com.zqh.blog.thread;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/7 17:39
 * description: spring 初始化接受后起后台线程
 **/
@Component
public class InitializingServer implements InitializingBean {

    @Autowired
    private LogToAPIThread logToAPIThread;
    @Autowired
    private LogToDBThread logToDBThread;

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread log2API = new Thread(logToAPIThread);
        log2API.start();

        Thread log2DB = new Thread(logToDBThread);
        log2DB.start();
    }
}
