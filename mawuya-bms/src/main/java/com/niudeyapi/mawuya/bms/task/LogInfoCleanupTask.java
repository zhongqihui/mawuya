/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.task;

import com.niudeyapi.mawuya.bms.config.LogInfoCleanupProperties;
import com.niudeyapi.mawuya.core.mapper.LogInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 访客日志清理定时任务。
 *
 * <h2>设计要点</h2>
 * <ul>
 *   <li><b>执行周期</b>：Cron 表达式来自 {@link LogInfoCleanupProperties#getCron()}，
 *       默认 {@code 0 0 3 * * ?}（每天凌晨 03:00 业务低峰时段）；</li>
 *   <li><b>保留策略</b>：保留最近 {@code retainDays}（默认 30）天的数据。基于
 *       {@code req_time < now - retainDays} 进行清理；{@code req_time} 是
 *       {@code yyyy-MM-dd HH:mm:ss.SSS} 字符串，字典序与时间序一致，
 *       且有 {@code idx_log_req_time} 索引；</li>
 *   <li><b>清理方式</b>：分批 {@code DELETE ... LIMIT batchSize}（默认 1000）循环执行，
 *       每批之间 sleep（默认 200ms）避免长事务/锁升级/主从延迟；
 *       单次任务最多跑 {@code maxBatches} 批（默认 200，即一次最多删 20 万条），
 *       兜底防止历史堆积时跑通宵；剩余的下一周期继续；</li>
 *   <li><b>异常处理</b>：单批失败按指数退避重试 {@code maxRetry} 次（默认 2，
 *       退避 1s → 2s）；多次失败后<strong>中断本轮</strong>但不抛出，等下一周期再尝试，
 *       避免短期故障（如主从切换、连接抖动）让 @Scheduled 调度器陷入异常状态；</li>
 *   <li><b>告警</b>：失败时打 ERROR 日志（带统一标记 {@code [log-cleanup][ALERT]}），
 *       便于运维侧日志采集做关键字告警规则；后续若接入企业微信/飞书/邮件通道，
 *       只需替换 {@link #alert(String, Throwable)} 内部实现；</li>
 *   <li><b>并发安全</b>：内部 {@link AtomicBoolean} 锁防止任务执行时间超出周期被并发触发
 *       （单实例够用；多实例部署需要外部分布式锁，见类末尾 TODO）。</li>
 * </ul>
 *
 * @author zqh
 */
@Component
public class LogInfoCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(LogInfoCleanupTask.class);

    /** req_time 格式与 {@code LogInterceptor} 写入保持一致 */
    private static final DateTimeFormatter REQ_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 业务统一时区：Asia/Shanghai。
     * <p>{@code LocalDateTime.now()} 不带参数时会用 JVM 默认时区；尽管启动类已经
     * 显式 {@code TimeZone.setDefault(Asia/Shanghai)}，这里再传入 ZoneId 作为
     * 防御性写法，避免任何第三方代码修改 default timezone 后导致清理 cutoff
     * 偏移 8 小时——一旦偏移会误删 8 小时内的真实日志或漏删该清理的旧日志。</p>
     */
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    /** 配置上下界，避免误配置打挂 DB */
    private static final int MIN_RETAIN_DAYS = 1;
    private static final int MIN_BATCH_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 10_000;
    private static final int MIN_MAX_BATCHES = 1;
    private static final int MAX_MAX_BATCHES = 100_000;
    private static final long MIN_BATCH_SLEEP_MS = 0L;
    private static final long MAX_BATCH_SLEEP_MS = 10_000L;
    private static final int MIN_RETRY = 0;
    private static final int MAX_RETRY = 5;
    private static final long MIN_BACKOFF_MS = 100L;
    private static final long MAX_BACKOFF_MS = 60_000L;

    /** 同一实例上的并发执行保护：true=正在运行 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final LogInfoMapper logInfoMapper;
    private final LogInfoCleanupProperties props;

    @Autowired
    public LogInfoCleanupTask(LogInfoMapper logInfoMapper, LogInfoCleanupProperties props) {
        this.logInfoMapper = logInfoMapper;
        this.props = props;
    }

    /**
     * 定时入口。Cron 由 SpEL 从配置读取；默认 {@code 0 0 3 * * ?}。
     *
     * <p>使用 SpEL 表达式 {@code ${...}} 让 yml 可热修改、灰度调度等；
     * 若想完全关闭定时调度，应通过 {@code mawuya.cleanup.log-info.enabled=false}
     * 直接 short-circuit，避免 Spring 把 enabled=false 也算成一个无效 cron。</p>
     */
    @Scheduled(cron = "${mawuya.cleanup.log-info.cron:0 0 3 * * ?}", zone = "Asia/Shanghai")
    public void scheduledClean() {
        if (!props.isEnabled()) {
            log.debug("[log-cleanup] disabled, skip");
            return;
        }
        // 单实例并发保护
        if (!running.compareAndSet(false, true)) {
            log.warn("[log-cleanup] previous run still in progress, skip this round");
            return;
        }
        try {
            doClean();
        } catch (Throwable t) {
            // 顶层兜底：@Scheduled 抛异常会被框架吞掉日志，这里显式告警
            alert("[log-cleanup][ALERT] unexpected error in scheduledClean", t);
        } finally {
            running.set(false);
        }
    }

    /** 核心清理逻辑：抽出来便于单元测试与手动触发。 */
    void doClean() {
        // 1. 收敛配置（防误配置）
        int retainDays = clamp(props.getRetainDays(), MIN_RETAIN_DAYS, Integer.MAX_VALUE);
        int batchSize  = clamp(props.getBatchSize(),  MIN_BATCH_SIZE,  MAX_BATCH_SIZE);
        int maxBatches = clamp(props.getMaxBatches(), MIN_MAX_BATCHES, MAX_MAX_BATCHES);
        long batchSleepMs = clampLong(props.getBatchSleepMs(), MIN_BATCH_SLEEP_MS, MAX_BATCH_SLEEP_MS);
        int  maxRetry  = clamp(props.getMaxRetry(),   MIN_RETRY,       MAX_RETRY);
        long backoffMs = clampLong(props.getRetryBackoffMs(), MIN_BACKOFF_MS, MAX_BACKOFF_MS);

        // 2. 计算截止时间，按字符串比较（与 req_time 同格式）
        //    显式传 Asia/Shanghai ZoneId，与 LogInterceptor 落库的 +08 时间字符串严格对齐，
        //    防止任何环境下 JVM 默认时区被修改导致 cutoff 偏移 8 小时。
        LocalDateTime cutoff = LocalDateTime.now(DEFAULT_ZONE).minusDays(retainDays);
        String reqTimeBefore = cutoff.format(REQ_TIME_FMT);

        long startNanos = System.nanoTime();

        // 3. 预统计（失败不阻断，仅用于日志可观测）
        int totalCandidate = -1;
        try {
            totalCandidate = logInfoMapper.countOlderThan(reqTimeBefore);
        } catch (Exception e) {
            log.warn("[log-cleanup] countOlderThan failed (ignored): {}", e.getMessage());
        }
        log.info("[log-cleanup] start retainDays={} batchSize={} maxBatches={} cutoff={} candidate~={}",
                retainDays, batchSize, maxBatches, reqTimeBefore, totalCandidate);

        // 4. 分批 DELETE 循环
        int totalDeleted = 0;
        int batches = 0;
        for (int i = 0; i < maxBatches; i++) {
            int deleted = deleteOneBatchWithRetry(reqTimeBefore, batchSize, maxRetry, backoffMs);
            if (deleted < 0) {
                // 单批彻底失败：本轮终止，等下个周期重试
                alert("[log-cleanup][ALERT] batch delete failed after " + maxRetry
                        + " retries, abort this round. totalDeleted=" + totalDeleted, null);
                break;
            }
            totalDeleted += deleted;
            batches++;
            if (deleted < batchSize) {
                // 命中 < limit 表示已删完，正常退出
                break;
            }
            sleepQuietly(batchSleepMs);
        }

        long elapsedMs = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
        log.info("[log-cleanup] done deleted={} batches={} elapsedMs={} cutoff={}",
                totalDeleted, batches, elapsedMs, reqTimeBefore);

        // 5. 如果跑满 maxBatches 还有残留，提示一下；下个周期会继续
        if (batches >= maxBatches && totalCandidate > totalDeleted) {
            log.warn("[log-cleanup] reached maxBatches={} but still have residue, will continue next round",
                    maxBatches);
        }
    }

    /**
     * 执行一个删除批次，带指数退避重试。
     *
     * @return 实际删除条数；返回负数表示重试用尽仍失败
     */
    private int deleteOneBatchWithRetry(String reqTimeBefore, int batchSize,
                                        int maxRetry, long backoffMs) {
        int attempt = 0;
        while (true) {
            try {
                return logInfoMapper.deleteOlderThan(reqTimeBefore, batchSize);
            } catch (Exception e) {
                if (attempt >= maxRetry) {
                    log.error("[log-cleanup] batch delete failed (attempt={}/{}): {}",
                            attempt + 1, maxRetry + 1, e.getMessage(), e);
                    return -1;
                }
                long sleep = backoffMs * (1L << attempt);
                log.warn("[log-cleanup] batch delete failed (attempt={}/{}), retry after {}ms: {}",
                        attempt + 1, maxRetry + 1, sleep, e.getMessage());
                sleepQuietly(sleep);
                attempt++;
            }
        }
    }

    /**
     * 失败告警。当前实现为 ERROR 日志输出（带 [ALERT] 关键字），
     * 由运维侧日志采集系统（如 Loki/ELK）通过关键字规则上报到企业微信/钉钉/邮件等渠道。
     *
     * <p>如未来要直连告警通道（飞书机器人 / 短信网关 / PagerDuty 等），
     * 仅需在此方法内追加发送逻辑即可，调用方无感知。</p>
     */
    private void alert(String msg, Throwable t) {
        if (!props.isAlertEnabled()) {
            log.warn("[log-cleanup] alert disabled, suppress: {}", msg);
            return;
        }
        if (t != null) {
            log.error(msg, t);
        } else {
            log.error(msg);
        }
        // TODO: 接入实际告警通道（企业微信 / 飞书 / 邮件 / SMS）时在此发送
    }

    /** 手动触发入口：供 BMS 控制台/单测调用，使用与定时调度同一套逻辑。 */
    public void runOnce() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("[log-cleanup] task already running");
        }
        try {
            doClean();
        } finally {
            running.set(false);
        }
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        return Math.min(v, max);
    }

    private static long clampLong(long v, long min, long max) {
        if (v < min) return min;
        return Math.min(v, max);
    }

    private static void sleepQuietly(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    // 多实例部署 TODO:
    //   当 bms 横向扩容到多实例时，AtomicBoolean 只在单 JVM 内有效，
    //   需要替换为基于 Redis (SET NX PX) 或 数据库行锁 的分布式锁，
    //   或者改用 ShedLock / Quartz cluster mode 接管调度。
}
