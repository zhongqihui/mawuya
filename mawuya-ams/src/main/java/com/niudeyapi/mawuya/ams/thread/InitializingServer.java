/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.thread;

import com.niudeyapi.mawuya.core.thread.LogToAPIThread;
import com.niudeyapi.mawuya.core.thread.LogToDBThread;
import com.niudeyapi.mawuya.core.thread.ReadNumToDBThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AMS 启动后初始化：通过 {@link ThreadPoolExecutor} 拉起 3 个守护后台线程。
 *
 * <p>遵循阿里规约：</p>
 * <ul>
 *   <li>禁止使用 {@code Executors} 创建线程池（默认无界队列 / 无界线程数 → OOM 风险）；
 *       改用 {@link ThreadPoolExecutor} 显式声明所有参数。</li>
 *   <li>禁止匿名 {@code new Thread(...)}：所有线程必须通过统一命名工厂创建，
 *       便于线上 jstack / arthas 排查。</li>
 *   <li>容器关闭时实现 {@link DisposableBean#destroy()} 通知 Runnable 退出，
 *       并对线程池做 {@code shutdownNow + awaitTermination} 优雅关闭。</li>
 * </ul>
 *
 * <p>仅在 ams 模块装载，bms 不引入此类。条件：{@code mawuya.async.enabled=true}。</p>
 *
 * @author 钟启辉
 */
@Component
@ConditionalOnProperty(prefix = "mawuya.async", name = "enabled", havingValue = "true")
public class InitializingServer implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(InitializingServer.class);

    /** 后台线程数：3 个固定 Runnable，对应核心数 = 最大数 = 3 */
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE  = 3;
    /** keepAlive 秒：core 线程不会闲置回收，仅在 max 临时线程上生效（此处与 core 相等不影响） */
    private static final long KEEP_ALIVE_SECONDS = 0L;
    /** 工作队列容量：避免无界队列导致内存膨胀 */
    private static final int QUEUE_CAPACITY = 16;
    /** 关闭等待超时（秒） */
    private static final long SHUTDOWN_AWAIT_SECONDS = 5L;

    @Autowired private LogToAPIThread    logToAPIThread;
    @Autowired private LogToDBThread     logToDBThread;
    @Autowired private ReadNumToDBThread readNumToDBThread;

    /** 后台守护线程池（不要求在容器关闭后继续运行，故 daemon=true） */
    private ThreadPoolExecutor backgroundExecutor;

    @Override
    public void afterPropertiesSet() {
        backgroundExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                new NamedThreadFactory("mawuya-async", true),
                // 任务被拒时记日志而不是悄悄丢弃；后台 Runnable 数量固定 3 个，理论上不会触发
                (r, executor) -> log.warn("[mawuya-async] task rejected: pool={}, queue={}",
                        executor.getPoolSize(), executor.getQueue().size()));

        backgroundExecutor.execute(logToAPIThread);
        backgroundExecutor.execute(logToDBThread);
        backgroundExecutor.execute(readNumToDBThread);
        log.info("[mawuya-async] background executor started: core={}, queue={}",
                CORE_POOL_SIZE, QUEUE_CAPACITY);
    }

    @Override
    public void destroy() throws InterruptedException {
        // 1) 先通知 Runnable 自身退出循环（实现里通过 AtomicBoolean.shutDown() 控制）
        if (logToAPIThread != null) {
            logToAPIThread.shutDown();
        }
        if (logToDBThread != null) {
            logToDBThread.shutDown();
        }
        if (readNumToDBThread != null) {
            readNumToDBThread.shutDown();
        }
        // 2) 再关闭线程池
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdownNow();
            if (!backgroundExecutor.awaitTermination(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("[mawuya-async] executor did not terminate within {}s", SHUTDOWN_AWAIT_SECONDS);
            } else {
                log.info("[mawuya-async] background executor shutdown gracefully");
            }
        }
    }

    /**
     * 命名 + daemon 控制的 {@link ThreadFactory}。
     * 线程名格式：{@code <prefix>-<seq>}，方便线上 jstack 一眼定位。
     */
    private static final class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final boolean daemon;
        private final AtomicInteger counter = new AtomicInteger(1);

        NamedThreadFactory(String prefix, boolean daemon) {
            this.prefix = prefix;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, prefix + "-" + counter.getAndIncrement());
            t.setDaemon(daemon);
            // 默认 NORM_PRIORITY，避免使用非默认优先级导致跨平台行为不一致
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
