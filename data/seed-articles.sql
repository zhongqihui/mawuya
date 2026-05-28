-- =====================================================================
-- Mawuya 测试文章种子数据
-- 用途：前台首页/分类/归档/详情/分页 功能验证
--
-- 执行方式：
--   mysql -h127.0.0.1 -P3306 -uroot -p123456 blog < data/seed-articles.sql
--
-- 脚本特性（幂等）：
--   1. 先按 article_title 删除可能已存在的种子文章，再重新插入；
--   2. 分类按 category_name 唯一键 upsert，不会覆盖业务真实数据；
--   3. 文章覆盖 5 个分类、3 个年份（用于归档），共 25 条，足以触发分页（pageSize=10）。
--
-- 数据状态分布：
--   - read_num / praise_num / tease_num / review_num 跨低中高三档
--   - picture_url 覆盖 3 种状态：无图(NULL)、单图、多图（分号分隔）
--   - article_content 覆盖短文 / 长文 / 含 emoji
-- =====================================================================

USE `blog`;

-- 强制连接字符集为 utf8mb4，确保 emoji（如 🌸 🚀）4 字节字符正确入库；
-- 否则 5.7 默认客户端字符集 utf8mb3 会把 emoji 误判为 6 字节序列从而触发 varchar 长度报错。
SET NAMES utf8mb4;

-- 1. 准备分类（幂等 upsert）
INSERT INTO `category_info` (`category_name`) VALUES
    ('未分类'), ('技术'), ('生活'), ('随笔'), ('读书')
    ON DUPLICATE KEY UPDATE `category_name` = VALUES(`category_name`);

-- 取出 5 个分类的 sn 备用（@cat_xxx 为会话变量，下文 INSERT 直接引用）
SET @cat_default = (SELECT sn FROM category_info WHERE category_name = '未分类' LIMIT 1);
SET @cat_tech    = (SELECT sn FROM category_info WHERE category_name = '技术'   LIMIT 1);
SET @cat_life    = (SELECT sn FROM category_info WHERE category_name = '生活'   LIMIT 1);
SET @cat_essay   = (SELECT sn FROM category_info WHERE category_name = '随笔'   LIMIT 1);
SET @cat_book    = (SELECT sn FROM category_info WHERE category_name = '读书'   LIMIT 1);

-- 2. 清理旧的种子数据（按标题前缀，避免误删人工数据）
DELETE FROM `article_info` WHERE `article_title` LIKE '[SEED]%';

-- 3. 批量插入 25 条种子文章
INSERT INTO `article_info`
    (`category_sn`, `read_num`, `review_num`, `praise_num`, `tease_num`,
     `picture_url`, `article_title`, `article_summary`, `article_content`,
     `insert_time`, `update_time`)
VALUES
-- 技术分类（10 条，覆盖各种状态）---------------------------------------
(@cat_tech,   1024, 12,  88,  2,  '/statics/images/blur_bg.png',
 '[SEED] Spring Boot 启动原理深度解析',
 '从 SpringApplication.run 到 Tomcat 启动的全链路梳理',
 '<p>Spring Boot 的启动流程主要包括：SpringApplication 实例化、环境准备、ApplicationContext 创建与刷新、内嵌容器启动等阶段...</p><p>本文将逐一拆解。</p>',
 '2024-03-15 09:00:00', '2024-03-15 09:00:00'),

(@cat_tech,    512,  6,  45,  1,  '/statics/images/blur_bg.png;/statics/images/bg.jpg',
 '[SEED] MyBatis 一二级缓存实战陷阱',
 'MyBatis 缓存看起来美好，实际生产用要谨慎',
 '<p>一级缓存默认开启在 SqlSession 范围内，二级缓存在 namespace 范围内...</p><p>多表查询、分布式部署都会导致脏数据。</p>',
 '2024-04-20 10:30:00', '2024-04-20 10:30:00'),

(@cat_tech,    256,  3,  20,  0,  NULL,
 '[SEED] Druid 连接池监控配置指南',
 '通过 StatFilter 与 StatViewServlet 启用监控大盘',
 '<p>Druid 自带的监控大盘功能强大，配置简单：在 application.yml 中开启 stat 即可。</p>',
 '2024-05-10 14:20:00', '2024-05-10 14:20:00'),

(@cat_tech,   2048, 24, 150,  5,  '/statics/images/blur_bg.png',
 '[SEED] JVM G1 GC 调优实战记录 🚀',
 '一次线上 Full GC 频繁问题的排查过程',
 '<p>使用 jstat / jmap / arthas 等工具进行内存分析...</p><p>最终通过调整 -XX:MaxGCPauseMillis 与 Region 大小解决。</p>',
 '2024-06-18 16:45:00', '2024-06-22 11:00:00'),

(@cat_tech,    320,  4,  30,  0,  NULL,
 '[SEED] Redis 分布式锁的几种实现',
 'SETNX / RedLock / Redisson 对比',
 '<p>SETNX 简单但有锁过期续期问题；RedLock 算法争议较多；Redisson 是工业界主流选择。</p>',
 '2024-08-05 11:00:00', '2024-08-05 11:00:00'),

(@cat_tech,    180,  2,  15,  1,  NULL,
 '[SEED] Thymeleaf 与 Spring Boot 集成',
 '从 JSP 迁移到 Thymeleaf 的实操经验',
 '<p>Thymeleaf 的 th:href、th:src、th:replace 三大利器，加上 @{} 链接表达式，足以替换 JSTL。</p>',
 '2024-09-12 13:00:00', '2024-09-12 13:00:00'),

(@cat_tech,    410,  5,  35,  0,  '/statics/images/blur_bg.png',
 '[SEED] HTTP/2 与 HTTP/3 的演进',
 '从队头阻塞到 QUIC，传输层的不断优化',
 '<p>HTTP/2 多路复用解决了 HTTP/1.1 的队头阻塞，但 TCP 队头阻塞仍存在；HTTP/3 基于 UDP 的 QUIC 彻底解决。</p>',
 '2025-01-08 09:30:00', '2025-01-08 09:30:00'),

(@cat_tech,     90,  1,   8,  0,  NULL,
 '[SEED] Maven 多模块工程实践',
 '父子 POM、dependencyManagement、scope 详解',
 '<p>合理使用 dependencyManagement 集中管理版本；scope 决定依赖在编译/测试/运行时的可见性。</p>',
 '2025-02-14 15:20:00', '2025-02-14 15:20:00'),

(@cat_tech,    760, 10,  62,  3,  '/statics/images/blur_bg.png',
 '[SEED] MySQL 索引失效的 12 种场景',
 '记一次因 OR 导致全表扫描的事故',
 '<p>本文整理常见索引失效场景：函数操作、隐式类型转换、OR 连接、!=、NOT IN、LIKE 通配符前置等。</p>',
 '2025-03-22 10:00:00', '2025-03-22 10:00:00'),

(@cat_tech,    150,  0,  12,  0,  NULL,
 '[SEED] Git rebase 与 merge 的取舍',
 '团队协作下保持提交历史整洁',
 '<p>rebase 能保持线性历史，但会改写提交；merge 保留分支痕迹，更安全。建议私有分支用 rebase，公共分支用 merge。</p>',
 '2025-05-01 12:00:00', '2025-05-01 12:00:00'),

-- 生活分类（5 条）-------------------------------------------------------
(@cat_life,    320,  8,  40,  2,  '/statics/images/blur_bg.png',
 '[SEED] 春日漫步：城市绿道的小确幸 🌸',
 '周末骑行三十公里的随手记录',
 '<p>木棉花开、白鹭低飞、咖啡冒着热气，城市的春天总在不经意间打动人。</p>',
 '2024-04-07 18:00:00', '2024-04-07 18:00:00'),

(@cat_life,    140,  3,  18,  0,  NULL,
 '[SEED] 在家做麻婆豆腐的关键三步',
 '豆瓣酱要炒香、淀粉要分两次勾芡',
 '<p>第一步：宽油下豆瓣酱炒出红油；第二步：豆腐冷水下锅焯掉豆腥；第三步：起锅前撒花椒粉。</p>',
 '2024-07-21 19:30:00', '2024-07-21 19:30:00'),

(@cat_life,    580, 11,  72,  3,  '/statics/images/blur_bg.png;/statics/images/bg.jpg',
 '[SEED] 京都自由行 5 日攻略',
 '从二条城到岚山：避开人潮的最佳时段',
 '<p>清晨 7 点的清水寺、傍晚的伏见稻荷大社，是体验京都的最佳时机。本攻略包含交通、住宿、餐饮全部内容。</p>',
 '2024-11-15 20:00:00', '2024-11-18 21:30:00'),

(@cat_life,     60,  0,   5,  0,  NULL,
 '[SEED] 跑步 100 天后的身体变化',
 '从 5 公里跑不动到完成半程马拉松',
 '<p>体重下降 8 公斤，静息心率降到 58；睡眠质量明显改善；最大的变化是心态——更平静。</p>',
 '2025-01-25 08:00:00', '2025-01-25 08:00:00'),

(@cat_life,    220,  4,  28,  1,  '/statics/images/blur_bg.png',
 '[SEED] 周末逛菜市场的乐趣',
 '比超市更接地气的城市切片',
 '<p>三块钱的小香葱、十二块的活鲈鱼，菜市场是观察城市烟火气的最佳去处。</p>',
 '2025-04-10 09:30:00', '2025-04-10 09:30:00'),

-- 随笔分类（4 条）-------------------------------------------------------
(@cat_essay,   100,  2,  10,  0,  NULL,
 '[SEED] 关于"忙"的反思',
 '我们究竟在忙什么？',
 '<p>每天回家瘫在沙发上，回想一天，竟说不出做了什么实质的事。这种"虚忙"才是最可怕的。</p>',
 '2024-02-29 22:00:00', '2024-02-29 22:00:00'),

(@cat_essay,    45,  1,   3,  0,  NULL,
 '[SEED] 一杯咖啡的时间',
 '在咖啡冷掉之前我能做什么？',
 '<p>大概可以读完一篇技术博客，可以回复三封邮件，也可以什么都不做。重点在于：自由选择本身。</p>',
 '2024-05-08 16:00:00', '2024-05-08 16:00:00'),

(@cat_essay,   175,  3,  22,  1,  NULL,
 '[SEED] 三十岁前后想明白的几件小事',
 '不再纠结，是种本事',
 '<p>1. 健康是最大的复利；2. 关系比成就重要；3. 不必证明给所有人看。</p>',
 '2024-12-30 23:30:00', '2024-12-30 23:30:00'),

(@cat_essay,    88,  1,   9,  0,  '/statics/images/blur_bg.png',
 '[SEED] 朋友圈，你还看吗',
 '从重度使用到几乎不刷',
 '<p>越来越喜欢长文与博客这种慢节奏表达，朋友圈对我已经变成只用来发布的工具。</p>',
 '2025-04-28 17:00:00', '2025-04-28 17:00:00'),

-- 读书分类（4 条）-------------------------------------------------------
(@cat_book,    260,  5,  30,  1,  '/statics/images/blur_bg.png',
 '[SEED] 《深入理解 Java 虚拟机》读书笔记',
 '从内存模型到字节码执行引擎',
 '<p>周志明老师的这本书值得反复读，每次都有新收获。本文整理第 2-5 章的关键脑图。</p>',
 '2024-06-30 21:00:00', '2024-06-30 21:00:00'),

(@cat_book,    310,  6,  38,  2,  NULL,
 '[SEED] 《被讨厌的勇气》：课题分离三日实践',
 '阿德勒心理学的可操作部分',
 '<p>把"别人怎么想"的课题还给别人，把"我怎么做"的课题留给自己。三天下来，焦虑确实减少。</p>',
 '2024-10-12 22:00:00', '2024-10-12 22:00:00'),

(@cat_book,    120,  2,  14,  0,  NULL,
 '[SEED] 《人月神话》经典重读',
 '为什么 50 年后仍未过时',
 '<p>"向落后的项目增加人手只会让项目更落后"，Brooks 定律在今天的研发管理中依然成立。</p>',
 '2025-02-02 10:00:00', '2025-02-02 10:00:00'),

(@cat_book,     35,  0,   2,  0,  NULL,
 '[SEED] 在读：《设计数据密集型应用》',
 '一本被业界称为分布式圣经的书',
 '<p>正在精读第 5 章 Replication，从单主到多主到无主三种模式的对比让人印象深刻。</p>',
 '2025-05-20 19:30:00', '2025-05-20 19:30:00'),

-- 未分类（2 条，验证 categorySn=0/默认分类的展示）---------------------
(@cat_default,  18,  0,   1,  0,  NULL,
 '[SEED] Hello World：第一篇博客',
 '从这里开始',
 '<p>新博客上线，先 Hello World 一下。后续会陆续补上技术与生活两大类内容。</p>',
 '2024-01-01 00:00:01', '2024-01-01 00:00:01'),

(@cat_default,   3,  0,   0,  0,  NULL,
 '[SEED] 测试草稿：占位文章',
 '这是一篇测试草稿，用于验证后台编辑流程',
 '<p>占位内容</p>',
 '2025-05-28 11:00:00', '2025-05-28 11:00:00');

-- 4. 验证：打印当前文章总数与分类汇总
SELECT 'TOTAL' AS metric, COUNT(*) AS value FROM article_info WHERE article_title LIKE '[SEED]%'
UNION ALL
SELECT CONCAT('CAT-', c.category_name), COUNT(a.sn)
FROM category_info c
LEFT JOIN article_info a ON a.category_sn = c.sn AND a.article_title LIKE '[SEED]%'
GROUP BY c.sn, c.category_name
ORDER BY metric;
