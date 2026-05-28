-- =====================================================================
-- Mawuya 博客系统数据库初始化脚本
-- 适用：MySQL 5.7+ / 8.x
-- 字符集：utf8mb4（支持 emoji）
-- 与 application.yml 中 spring.datasource.url 的 schema 名 `blog` 对齐
-- =====================================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `blog`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `blog`;

-- 强制连接字符集为 utf8mb4：MySQL 5.7 默认客户端字符集为 utf8mb3，
-- 不显式声明会让中文/emoji 写入时被错误编码，造成乱码或 varchar 长度溢出。
SET NAMES utf8mb4;

-- 2. 文章信息表
DROP TABLE IF EXISTS `article_info`;
CREATE TABLE `article_info` (
    `sn`              INT(11)        NOT NULL AUTO_INCREMENT COMMENT '主键sn',
    `category_sn`     INT(11)        NOT NULL DEFAULT 0      COMMENT '分类主键',
    `read_num`        INT(11)        NOT NULL DEFAULT 0      COMMENT '阅读次数',
    `review_num`      INT(11)        NOT NULL DEFAULT 0      COMMENT '评论次数',
    `praise_num`      INT(11)        NOT NULL DEFAULT 0      COMMENT '赞次数',
    `tease_num`       INT(11)        NOT NULL DEFAULT 0      COMMENT '踩次数',
    `picture_url`     VARCHAR(1000)  DEFAULT NULL            COMMENT '图片url，多张图片路径用分号隔开',
    `article_title`   VARCHAR(120)   NOT NULL                COMMENT '文章标题',
    `article_content` MEDIUMTEXT     DEFAULT NULL            COMMENT '文章内容',
    `article_summary` VARCHAR(300)   DEFAULT NULL            COMMENT '文章概要',
    `insert_time`     DATETIME       DEFAULT NULL            COMMENT '插入时间',
    `update_time`     DATETIME       DEFAULT NULL            COMMENT '修改时间',
    PRIMARY KEY (`sn`),
    KEY `idx_article_category_sn` (`category_sn`),
    KEY `idx_article_insert_time` (`insert_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章信息表';

-- 3. 评论信息表
DROP TABLE IF EXISTS `review_info`;
CREATE TABLE `review_info` (
    `sn`             INT(11)      NOT NULL AUTO_INCREMENT COMMENT '主键sn',
    `article_sn`     INT(11)      NOT NULL                COMMENT '评论文章的主键sn',
    `psn`            INT(11)      NOT NULL DEFAULT 0      COMMENT '该评论的父节点sn',
    `csn`            INT(11)      DEFAULT NULL            COMMENT '该评论的子节点',
    `praise_num`     INT(11)      NOT NULL DEFAULT 0      COMMENT '赞次数',
    `tease_num`      INT(11)      NOT NULL DEFAULT 0      COMMENT '踩次数',
    `review_name`    VARCHAR(40)  DEFAULT NULL            COMMENT '评论人的名字',
    `review_content` VARCHAR(140) DEFAULT NULL            COMMENT '评论的内容',
    `review_date`    DATETIME     DEFAULT NULL            COMMENT '评论的时间',
    PRIMARY KEY (`sn`),
    KEY `idx_review_article_sn` (`article_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论信息表';

-- 4. 分类信息表
DROP TABLE IF EXISTS `category_info`;
CREATE TABLE `category_info` (
    `sn`            INT(11)     NOT NULL AUTO_INCREMENT COMMENT '分类表主键sn',
    `category_name` VARCHAR(20) NOT NULL                COMMENT '分类名称',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分类表';

-- 5. 博客信息表
DROP TABLE IF EXISTS `blog_info`;
CREATE TABLE `blog_info` (
    `blogger_name` VARCHAR(30) DEFAULT NULL COMMENT '博主名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客信息表';

-- 6. 访客记录表
DROP TABLE IF EXISTS `log_info`;
CREATE TABLE `log_info` (
    `sn`              INT(11)       NOT NULL AUTO_INCREMENT COMMENT '主键sn',
    `ip_addr`         VARCHAR(50)   DEFAULT NULL            COMMENT '访客ip地址',
    `country`         VARCHAR(50)   DEFAULT NULL            COMMENT '访客国家',
    `province`        VARCHAR(50)   DEFAULT NULL            COMMENT '访客省份',
    `city`            VARCHAR(50)   DEFAULT NULL            COMMENT '访客城市',
    `area`            VARCHAR(50)   DEFAULT NULL            COMMENT '区/县',
    `detail_position` VARCHAR(100)  DEFAULT NULL            COMMENT '详细地理位置',
    `isp`             VARCHAR(50)   DEFAULT NULL            COMMENT '访客ip运营商',
    `try_times`       INT(1)        NOT NULL DEFAULT 0      COMMENT '推送次数',
    `req_time`        VARCHAR(50)   DEFAULT NULL            COMMENT '访客访问时间',
    `resp_time`       VARCHAR(50)   DEFAULT NULL            COMMENT '响应访客时间',
    `consume_time`    VARCHAR(30)   DEFAULT NULL            COMMENT '该次请求消耗时间',
    `req_url`         VARCHAR(200)  DEFAULT NULL            COMMENT '请求路径',
    `req_method`      VARCHAR(10)   DEFAULT NULL            COMMENT '请求方式，post，get...',
    `params`          VARCHAR(500)  DEFAULT NULL            COMMENT '请求参数',
    `browser`         VARCHAR(100)  DEFAULT NULL            COMMENT '访客浏览器信息',
    `resp_status`     VARCHAR(1)    DEFAULT NULL            COMMENT '响应状态：0 成功；1 失败',
    `except_message`  VARCHAR(1000) DEFAULT NULL            COMMENT '异常错误信息',
    PRIMARY KEY (`sn`),
    KEY `idx_log_ip_addr` (`ip_addr`),
    KEY `idx_log_req_time` (`req_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='访客记录表';

-- 7. 初始化默认分类（让前台 categories 列表非空）
INSERT INTO `category_info` (`category_name`) VALUES
    ('未分类'),
    ('技术'),
    ('生活')
    ON DUPLICATE KEY UPDATE `category_name` = VALUES(`category_name`);
