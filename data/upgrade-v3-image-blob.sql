-- =====================================================================
-- v3 升级脚本：图片改用数据库存储
-- 1) 新增 image_blob 表，存储图片二进制 + 元数据
-- 2) 通过 /image/db/{id} 接口流式读取
-- 注意：MySQL 5.7 客户端默认 utf8mb3，必须显式 utf8mb4
-- =====================================================================
USE `blog`;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `image_blob`;
CREATE TABLE `image_blob` (
    `sn`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_name`     VARCHAR(160) NOT NULL                COMMENT '原始文件名（含扩展名）',
    `content_type`  VARCHAR(60)  NOT NULL                COMMENT 'MIME，例如 image/png',
    `byte_size`     BIGINT(20)   NOT NULL DEFAULT 0      COMMENT '字节大小',
    -- 图片二进制：MEDIUMBLOB 上限 16MB，已大于业务限制 10MB
    `data`          MEDIUMBLOB   NOT NULL                COMMENT '图片二进制',
    `sha256`        CHAR(64)     DEFAULT NULL            COMMENT 'SHA-256 摘要，用于去重',
    `source_url`    VARCHAR(500) DEFAULT NULL            COMMENT '源 URL（迁移时记录）',
    `created_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_sha256` (`sha256`),
    KEY `idx_created_time` (`created_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '图片二进制存储表';
