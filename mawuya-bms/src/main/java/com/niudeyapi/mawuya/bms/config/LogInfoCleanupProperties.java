/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 访客日志清理定时任务配置。
 * <p>对应 yml 路径：{@code mawuya.cleanup.log-info.*}。所有字段都有合理默认值，
 * 即使 yml 不配置也能按"每天凌晨 3 点保留 30 天 / 单批 1000 条"运行。</p>
 *
 * <p>配置项一览：</p>
 * <ul>
 *   <li>{@code enabled}        - 总开关，默认 {@code true}</li>
 *   <li>{@code cron}           - Cron 表达式，默认 {@code 0 0 3 * * ?}（每天 03:00）</li>
 *   <li>{@code retainDays}     - 保留天数，默认 30；早于此值的记录会被清理</li>
 *   <li>{@code batchSize}      - 单批 DELETE 上限，默认 1000；防长事务</li>
 *   <li>{@code maxBatches}     - 单次任务最大批次数，默认 200（兜底，避免堆积时跑通宵）</li>
 *   <li>{@code batchSleepMs}   - 批与批之间睡眠毫秒，默认 200，给主从同步留缓冲</li>
 *   <li>{@code maxRetry}       - 单批失败重试次数，默认 2（共最多 3 次尝试）</li>
 *   <li>{@code retryBackoffMs} - 重试退避基数毫秒，默认 1000；指数退避 base*2^n</li>
 *   <li>{@code alertEnabled}   - 失败告警开关，默认 {@code true}；当前以 ERROR 日志输出，
 *       后续接入企业微信 / 飞书 / 邮件等告警通道时改这里即可</li>
 * </ul>
 *
 * @author zqh
 */
@Component
@ConfigurationProperties(prefix = "mawuya.cleanup.log-info")
public class LogInfoCleanupProperties {

    /** 总开关。关闭后任务方法依旧被 @Scheduled 触发，但会直接 return。 */
    private boolean enabled = true;

    /** Spring Cron 表达式：秒 分 时 日 月 周。默认每天 03:00 执行。 */
    private String cron = "0 0 3 * * ?";

    /** 保留天数，删除 now-retainDays 之前的数据。下限 1。 */
    private int retainDays = 30;

    /** 单批 DELETE 行数上限，下限 100，上限 10000。 */
    private int batchSize = 1000;

    /** 单次任务最多执行多少批，避免堆积时无限循环。下限 1，上限 100000。 */
    private int maxBatches = 200;

    /** 批与批之间睡眠毫秒，给 IO/主从同步留缓冲。下限 0，上限 10000。 */
    private long batchSleepMs = 200L;

    /** 单批失败重试次数，下限 0，上限 5。 */
    private int maxRetry = 2;

    /** 重试退避基数毫秒，实际延迟 = base * 2^attempt，下限 100，上限 60000。 */
    private long retryBackoffMs = 1000L;

    /** 失败告警开关；启用后失败时打 ERROR 级别日志，便于日志采集层做规则告警。 */
    private boolean alertEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public LogInfoCleanupProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getCron() {
        return cron;
    }

    public LogInfoCleanupProperties setCron(String cron) {
        this.cron = cron;
        return this;
    }

    public int getRetainDays() {
        return retainDays;
    }

    public LogInfoCleanupProperties setRetainDays(int retainDays) {
        this.retainDays = retainDays;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public LogInfoCleanupProperties setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public int getMaxBatches() {
        return maxBatches;
    }

    public LogInfoCleanupProperties setMaxBatches(int maxBatches) {
        this.maxBatches = maxBatches;
        return this;
    }

    public long getBatchSleepMs() {
        return batchSleepMs;
    }

    public LogInfoCleanupProperties setBatchSleepMs(long batchSleepMs) {
        this.batchSleepMs = batchSleepMs;
        return this;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public LogInfoCleanupProperties setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    public long getRetryBackoffMs() {
        return retryBackoffMs;
    }

    public LogInfoCleanupProperties setRetryBackoffMs(long retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
        return this;
    }

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public LogInfoCleanupProperties setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
        return this;
    }
}
