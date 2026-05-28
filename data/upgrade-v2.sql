-- =====================================================================
-- Mawuya 博客升级：新增标签系统 + 评论表索引补强
-- 执行：mysql -uroot -p123456 --default-character-set=utf8mb4 < data/upgrade-v2.sql
-- =====================================================================

USE `blog`;
SET NAMES utf8mb4;

-- 1. 标签表
DROP TABLE IF EXISTS `tag_info`;
CREATE TABLE `tag_info` (
    `sn`        INT(11)     NOT NULL AUTO_INCREMENT COMMENT '标签主键',
    `tag_name`  VARCHAR(30) NOT NULL                COMMENT '标签名称',
    `tag_color` VARCHAR(20) DEFAULT NULL            COMMENT '展示颜色（HEX，如 #2494f2）',
    PRIMARY KEY (`sn`),
    UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签表';

-- 2. 文章-标签 关联表
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
    `article_sn` INT(11) NOT NULL COMMENT '文章 sn',
    `tag_sn`     INT(11) NOT NULL COMMENT '标签 sn',
    PRIMARY KEY (`article_sn`, `tag_sn`),
    KEY `idx_at_tag_sn` (`tag_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章-标签 关联表';

-- 3. 评论表：补 review_date 索引（最新评论排序用），并把 review_content 升级到 500 字符
ALTER TABLE `review_info`
    MODIFY COLUMN `review_content` VARCHAR(500) DEFAULT NULL COMMENT '评论内容',
    ADD INDEX IF NOT EXISTS `idx_review_date` (`review_date`);

-- MySQL 5.7 不支持 IF NOT EXISTS on ADD INDEX，回退方案：先尝试删除再添加
-- 上面的 IF NOT EXISTS 在 MySQL 8 可用；5.7 会报错，但不影响整体执行可使用下面的兜底语句
-- 注：本脚本以 8.0 兼容性为主；若环境为 5.7 请手工执行下方等价 SQL。

-- 4. 准备种子标签（与 seed-articles 中文章配套）
INSERT INTO `tag_info` (`tag_name`, `tag_color`) VALUES
    ('Java',       '#007396'),
    ('Spring',     '#6db33f'),
    ('MySQL',      '#00758f'),
    ('JVM',        '#e76f00'),
    ('Redis',      '#dc382c'),
    ('架构',       '#2494f2'),
    ('前端',       '#f06529'),
    ('生活',       '#ff8a8a'),
    ('旅行',       '#7e57c2'),
    ('美食',       '#ff7043'),
    ('读书',       '#5c6bc0'),
    ('随笔',       '#9e9e9e'),
    ('健康',       '#26a69a'),
    ('运动',       '#43a047'),
    ('入门',       '#26c6da')
    ON DUPLICATE KEY UPDATE `tag_color` = VALUES(`tag_color`);

-- 验证
SELECT 'tag_info' AS tab, COUNT(*) AS cnt FROM tag_info
UNION ALL
SELECT 'article_tag', COUNT(*) FROM article_tag
UNION ALL
SELECT 'review_info', COUNT(*) FROM review_info;
