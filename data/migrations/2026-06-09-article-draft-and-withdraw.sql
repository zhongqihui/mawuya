-- =====================================================================
-- Migration: 文章草稿 & 撤回功能
-- Date    : 2026-06-09
-- Author  : 钟启辉
--
-- 背景：
--   - 新增 article_info.status 列：0=草稿 / 1=已发布（默认） / 2=已撤回
--   - 新建 article_status_log 表：记录草稿 → 发布 / 已发布 → 撤回 / 已撤回 → 重新发布
--     等全部状态流转事件，便于事后审计与时间线展示。
--   - 既有线上文章未带 status，统一升级为 1（已发布），保持对外可见行为不变。
--
-- 影响面：
--   - AMS（前台）所有只读路径已改为强制按 status=1 过滤；
--     升级 SQL 后旧数据被默认置 1，AMS 行为保持向后兼容。
--   - BMS（后台）列表新增「全部/草稿/已发布/已撤回」tab；编辑页根据状态展示
--     「保存草稿/发布」「撤回」「重新发布」等动作。
--
-- 回滚（紧急时执行）：
--   ALTER TABLE article_info DROP INDEX idx_article_status;
--   ALTER TABLE article_info DROP COLUMN status;
--   DROP TABLE article_status_log;
-- =====================================================================

USE `blog`;
SET NAMES utf8mb4;

-- 1) 给 article_info 增加 status 列与索引（幂等：用 INFORMATION_SCHEMA 判存在性）
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'article_info'
      AND COLUMN_NAME = 'status'
);

SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `article_info` ADD COLUMN `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT ''文章状态：0=草稿 / 1=已发布 / 2=已撤回'' AFTER `article_summary`',
    'SELECT ''column article_info.status already exists, skip'' AS info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'article_info'
      AND INDEX_NAME = 'idx_article_status'
);

SET @ddl := IF(@idx_exists = 0,
    'CREATE INDEX `idx_article_status` ON `article_info` (`status`)',
    'SELECT ''index article_info.idx_article_status already exists, skip'' AS info'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 老数据保险：把现有所有文章显式标为「已发布」，行为与升级前完全一致
UPDATE `article_info` SET `status` = 1 WHERE `status` IS NULL;

-- 2) 状态流转日志表（幂等）
CREATE TABLE IF NOT EXISTS `article_status_log` (
    `sn`           BIGINT(20)   NOT NULL AUTO_INCREMENT             COMMENT '主键',
    `article_sn`   INT(11)      NOT NULL                            COMMENT '关联 article_info.sn',
    `from_status`  TINYINT(4)   DEFAULT NULL                        COMMENT '变更前状态（首次创建为 NULL）',
    `to_status`    TINYINT(4)   NOT NULL                            COMMENT '变更后状态',
    `action`       VARCHAR(20)  NOT NULL                            COMMENT '动作：CREATE_DRAFT/PUBLISH/UPDATE_DRAFT/UPDATE_PUBLISHED/WITHDRAW/REPUBLISH',
    `operator`     VARCHAR(40)  DEFAULT NULL                        COMMENT '操作人（取 BMS 当前登录用户名）',
    `remark`       VARCHAR(200) DEFAULT NULL                        COMMENT '备注',
    `change_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '发生时间',
    PRIMARY KEY (`sn`),
    KEY `idx_asl_article_sn` (`article_sn`),
    KEY `idx_asl_change_time` (`change_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章状态流转日志';
