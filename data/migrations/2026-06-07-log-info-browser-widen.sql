-- =============================================================================
-- 迁移：log_info.browser 字段从 VARCHAR(100) 扩到 VARCHAR(500)
-- -----------------------------------------------------------------------------
-- 背景：
--   LogInterceptor.getOsAndBrowserInfo() 在 UA 不匹配已知模式时会 fallback 成
--   "Unknown-" + userAgent（取前 60 字符），并拼成 "OS,Browser" 两段。
--   现代浏览器（特别是 Chrome on Android、iOS Safari 等）UA 字符串经常
--   超过 200 字符；旧 VARCHAR(100) 在极端情况下仍可能被截断，导致
--   MysqlDataTruncation: Data too long for column 'browser' at row 1。
--
-- 修复策略：
--   1) 业务层（LogInterceptor）：fallback 前已 truncate(ua, 60)，最终再
--      truncate(整段, 480)，杜绝任何环境下的写入失败。
--   2) 物理表：把 browser 列扩到 VARCHAR(500)，为代码层 480 上限留 20 字符余量。
--
-- 执行方式：
--   线上：mysql -h <host> -u <user> -p <db> < 2026-06-07-log-info-browser-widen.sql
--   本地：mysql -uroot -p mawuya < data/migrations/2026-06-07-log-info-browser-widen.sql
-- =============================================================================

ALTER TABLE `log_info`
    MODIFY COLUMN `browser` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent 解析后的"OS,Browser"（含未识别 UA 截断）';
