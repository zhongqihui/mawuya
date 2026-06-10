-- =====================================================================
-- Migration: 日志管理表新增「请求头」字段
-- Date    : 2026-06-10
-- Author  : 钟启辉
--
-- 背景：
--   线上排障时常常需要看 Referer / User-Agent / Cookie / Origin / Host
--   等完整请求头，而原 log_info 仅保存了 UA 解析后的 OS,Browser 摘要，
--   无法满足追查需求（CSRF、爬虫、代理链路等场景尤其需要）。
--
-- 修复策略：
--   1) 新增 log_info.req_headers TEXT 列，业务层在拦截器 preHandle 时
--      把 HttpServletRequest 的全部 header 序列化为
--      "Header-Name: value\nHeader-Name2: value\n" 文本写入；
--   2) 敏感字段（Authorization / Cookie / Proxy-Authorization）做掩码，
--      避免管理后台日志页明文展示 token/cookie。
--   3) TEXT（65535 字节）足够装下任何正常浏览器的 header 总和；
--      代码层仍做 8KB 兜底截断，防止恶意巨型 header 撑爆字段。
--
-- 影响面：
--   - 写：新字段 NULL 兼容历史数据，Mapper 改成显式列出 17→18 字段；
--   - 读：BMS 日志详情弹窗新增「请求头」行展示；列表不展示（信息量大）。
--
-- 回滚（紧急时执行）：
--   ALTER TABLE log_info DROP COLUMN req_headers;
-- =====================================================================

USE `blog`;
SET NAMES utf8mb4;

-- 幂等新增 req_headers 列
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'log_info'
      AND COLUMN_NAME = 'req_headers'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `log_info` ADD COLUMN `req_headers` TEXT DEFAULT NULL COMMENT ''完整请求头（多行 Name: value，敏感字段已掩码）'' AFTER `browser`',
    'SELECT ''column log_info.req_headers already exists, skip'' AS info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
