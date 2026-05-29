-- =====================================================================
-- Mawuya 博客系统 · 数据库建表脚本
--
-- 适用：MySQL 5.7+ / 8.x；字符集 utf8mb4（兼容 emoji）
-- 用途：从空环境一次性建好 blog 库 + 全部业务表 + 必要的种子数据
--
-- 执行方式：
--   mysql -uroot -p123456 -h127.0.0.1 < data/schema.sql
--
-- 注意事项：
--   1. 脚本是「全量重建」语义：所有表都先 DROP IF EXISTS 再 CREATE，
--      正式环境慎用，仅适合开发 / 测试 / 首次安装；
--   2. 必须显式 SET NAMES utf8mb4，否则 MySQL 5.7 默认客户端为 utf8mb3，
--      中文 / emoji 写入会乱码或触发 varchar 长度溢出；
--   3. blog_info 是单例表（id 固定为 1），承载站点级配置（博主名、当前主题）；
--   4. sys_user / sys_role / sys_user_role 是 BMS 端 Spring Security 的鉴权表，
--      AMS 不依赖；admin 默认密码为 123456（BCrypt strength=10），生产请重置。
-- =====================================================================

-- 1. 建库
CREATE DATABASE IF NOT EXISTS `blog`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `blog`;
SET NAMES utf8mb4;

-- =====================================================================
-- 业务表
-- =====================================================================

-- 文章信息表
DROP TABLE IF EXISTS `article_info`;
CREATE TABLE `article_info` (
    `sn`              INT(11)        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_sn`     INT(11)        NOT NULL DEFAULT 0      COMMENT '分类主键（关联 category_info.sn）',
    `read_num`        INT(11)        NOT NULL DEFAULT 0      COMMENT '阅读次数',
    `review_num`      INT(11)        NOT NULL DEFAULT 0      COMMENT '评论次数（仅统计已审核通过）',
    `praise_num`      INT(11)        NOT NULL DEFAULT 0      COMMENT '点赞次数',
    `tease_num`       INT(11)        NOT NULL DEFAULT 0      COMMENT '点踩次数',
    `picture_url`     VARCHAR(1000)  DEFAULT NULL            COMMENT '封面图 URL，多张以分号分隔',
    `article_title`   VARCHAR(120)   NOT NULL                COMMENT '标题',
    `article_content` MEDIUMTEXT     DEFAULT NULL            COMMENT '正文（HTML）',
    `article_summary` VARCHAR(300)   DEFAULT NULL            COMMENT '摘要',
    `insert_time`     DATETIME       DEFAULT NULL            COMMENT '发布时间',
    `update_time`     DATETIME       DEFAULT NULL            COMMENT '最近修改时间',
    PRIMARY KEY (`sn`),
    KEY `idx_article_category_sn` (`category_sn`),
    KEY `idx_article_insert_time` (`insert_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章信息表';

-- 评论信息表
DROP TABLE IF EXISTS `review_info`;
CREATE TABLE `review_info` (
    `sn`             INT(11)      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `article_sn`     INT(11)      NOT NULL                COMMENT '所属文章 sn',
    `psn`            INT(11)      NOT NULL DEFAULT 0      COMMENT '父评论 sn（0 表示根评论）',
    `csn`            INT(11)      DEFAULT NULL            COMMENT '子评论 sn（备用字段）',
    `praise_num`     INT(11)      NOT NULL DEFAULT 0      COMMENT '点赞次数',
    `tease_num`      INT(11)      NOT NULL DEFAULT 0      COMMENT '点踩次数',
    `review_name`    VARCHAR(40)  DEFAULT NULL            COMMENT '评论者昵称',
    `review_content` VARCHAR(500) DEFAULT NULL            COMMENT '评论内容',
    `review_date`    DATETIME     DEFAULT NULL            COMMENT '评论时间',
    `review_status`  TINYINT(4)   NOT NULL DEFAULT 0      COMMENT '审核状态：0=待审核 / 1=已通过 / 2=已拒绝',
    PRIMARY KEY (`sn`),
    KEY `idx_review_article_sn` (`article_sn`),
    KEY `idx_review_status` (`review_status`),
    KEY `idx_review_date` (`review_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论信息表';

-- 分类信息表
DROP TABLE IF EXISTS `category_info`;
CREATE TABLE `category_info` (
    `sn`            INT(11)     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_name` VARCHAR(20) NOT NULL                COMMENT '分类名称',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- 标签信息表
DROP TABLE IF EXISTS `tag_info`;
CREATE TABLE `tag_info` (
    `sn`        INT(11)     NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_name`  VARCHAR(30) NOT NULL                COMMENT '标签名称',
    `tag_color` VARCHAR(20) DEFAULT NULL            COMMENT '展示颜色（HEX，如 #2494f2）',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';

-- 文章-标签 多对多关联表
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
    `article_sn` INT(11) NOT NULL COMMENT '文章 sn',
    `tag_sn`     INT(11) NOT NULL COMMENT '标签 sn',
    PRIMARY KEY (`article_sn`, `tag_sn`),
    KEY `idx_at_tag_sn` (`tag_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章-标签 关联表';

-- 博客信息表（单例：固定一行 id=1，承载站点级配置）
DROP TABLE IF EXISTS `blog_info`;
CREATE TABLE `blog_info` (
    `id`           INT          NOT NULL DEFAULT 1                COMMENT '单例主键，固定为 1',
    `blogger_name` VARCHAR(30)  DEFAULT NULL                       COMMENT '博主名',
    `theme_code`   VARCHAR(40)  NOT NULL DEFAULT 'default'         COMMENT 'AMS 当前主题：default / tech-dark / gradient-vivid / minimal-business',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客信息表（单例）';

-- 图片二进制存储表（v3 起所有图片改入库，避免依赖文件系统）
DROP TABLE IF EXISTS `image_blob`;
CREATE TABLE `image_blob` (
    `sn`            BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `file_name`     VARCHAR(160) NOT NULL                COMMENT '原始文件名（含扩展名）',
    `content_type`  VARCHAR(60)  NOT NULL                COMMENT 'MIME，例如 image/png',
    `byte_size`     BIGINT(20)   NOT NULL DEFAULT 0      COMMENT '字节大小',
    -- MEDIUMBLOB 上限 16MB，已大于业务限制 10MB
    `data`          MEDIUMBLOB   NOT NULL                COMMENT '图片二进制内容',
    `sha256`        CHAR(64)     DEFAULT NULL            COMMENT 'SHA-256 摘要，用于去重',
    `source_url`    VARCHAR(500) DEFAULT NULL            COMMENT '历史源 URL（迁移 / 引用追踪用）',
    `created_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_sha256` (`sha256`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片二进制存储表';

-- 访客访问日志表
DROP TABLE IF EXISTS `log_info`;
CREATE TABLE `log_info` (
    `sn`              INT(11)       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `ip_addr`         VARCHAR(50)   DEFAULT NULL            COMMENT '访客 IP',
    `country`         VARCHAR(50)   DEFAULT NULL            COMMENT '国家',
    `province`        VARCHAR(50)   DEFAULT NULL            COMMENT '省份',
    `city`            VARCHAR(50)   DEFAULT NULL            COMMENT '城市',
    `area`            VARCHAR(50)   DEFAULT NULL            COMMENT '区/县',
    `detail_position` VARCHAR(100)  DEFAULT NULL            COMMENT '详细地理位置',
    `isp`             VARCHAR(50)   DEFAULT NULL            COMMENT '运营商',
    `try_times`       INT(1)        NOT NULL DEFAULT 0      COMMENT '地理 API 重试次数',
    `req_time`        VARCHAR(50)   DEFAULT NULL            COMMENT '请求到达时间',
    `resp_time`       VARCHAR(50)   DEFAULT NULL            COMMENT '响应返回时间',
    `consume_time`    VARCHAR(30)   DEFAULT NULL            COMMENT '请求耗时',
    `req_url`         VARCHAR(200)  DEFAULT NULL            COMMENT '请求 URL',
    `req_method`      VARCHAR(10)   DEFAULT NULL            COMMENT '请求方法（GET/POST/...）',
    `params`          VARCHAR(500)  DEFAULT NULL            COMMENT '请求参数',
    `browser`         VARCHAR(100)  DEFAULT NULL            COMMENT 'User-Agent',
    `resp_status`     VARCHAR(1)    DEFAULT NULL            COMMENT '响应状态：0=成功 / 1=失败',
    `except_message`  VARCHAR(1000) DEFAULT NULL            COMMENT '异常信息（如有）',
    PRIMARY KEY (`sn`),
    KEY `idx_log_ip_addr` (`ip_addr`),
    KEY `idx_log_req_time` (`req_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客访问日志表';

-- =====================================================================
-- 鉴权表（仅 BMS 启用 Spring Security；AMS 匿名访问无需登录）
-- =====================================================================

-- 系统用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `sn`             INT(11)      NOT NULL AUTO_INCREMENT             COMMENT '主键',
    `username`       VARCHAR(40)  NOT NULL                            COMMENT '登录账号',
    `password_hash`  VARCHAR(80)  NOT NULL                            COMMENT 'BCrypt 加密后密码',
    `nickname`       VARCHAR(40)  DEFAULT NULL                        COMMENT '展示昵称',
    `email`          VARCHAR(80)  DEFAULT NULL                        COMMENT '邮箱',
    `enabled`        TINYINT(1)   NOT NULL DEFAULT 1                  COMMENT '是否启用：1=启用 / 0=禁用',
    `last_login_at`  DATETIME     DEFAULT NULL                        COMMENT '最近登录时间',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP         COMMENT '更新时间',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 系统角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `sn`          INT(11)      NOT NULL AUTO_INCREMENT             COMMENT '主键',
    `code`        VARCHAR(40)  NOT NULL                            COMMENT '角色 code，例如 ADMIN/EDITOR',
    `name`        VARCHAR(60)  NOT NULL                            COMMENT '显示名',
    `description` VARCHAR(200) DEFAULT NULL                        COMMENT '角色描述',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- 用户-角色 多对多关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_sn` INT(11) NOT NULL COMMENT 'sys_user.sn',
    `role_sn` INT(11) NOT NULL COMMENT 'sys_role.sn',
    PRIMARY KEY (`user_sn`, `role_sn`),
    KEY `idx_ur_role_sn` (`role_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- =====================================================================
-- 必要的种子数据（最小可运行集）
--   - 默认分类：让 AMS 分类列表非空
--   - blog_info 单例：theme_code=default
--   - 默认角色 + admin 账号（密码 123456）：让 BMS 能首次登录
-- =====================================================================

INSERT INTO `category_info` (`category_name`) VALUES
    ('未分类'),
    ('技术'),
    ('生活')
ON DUPLICATE KEY UPDATE `category_name` = VALUES(`category_name`);

INSERT INTO `blog_info` (`id`, `theme_code`) VALUES (1, 'default')
ON DUPLICATE KEY UPDATE `id` = `id`;

INSERT INTO `sys_role` (`code`, `name`, `description`) VALUES
    ('ADMIN',  '超级管理员', '拥有全部权限，包含用户与角色管理'),
    ('EDITOR', '内容编辑',   '可管理博客 / 评论 / 图片等内容，无用户管理权限')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 默认账号：admin / 123456 （首次登录后请立即修改）
INSERT INTO `sys_user` (`username`, `password_hash`, `nickname`, `enabled`) VALUES
    ('admin', '$2a$10$c81uRtr1MkvT3qSgIpKZOuiw94vXF26sdwyt/vcQMihN1vIYMsWCW', '管理员', 1)
ON DUPLICATE KEY UPDATE `username` = `username`;

-- 给 admin 绑定 ADMIN 角色
INSERT INTO `sys_user_role` (`user_sn`, `role_sn`)
SELECT u.sn, r.sn
FROM `sys_user` u, `sys_role` r
WHERE u.username = 'admin' AND r.code = 'ADMIN'
ON DUPLICATE KEY UPDATE `user_sn` = `user_sn`;
