/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.seed;

import com.mawuya.core.dataobject.ArticleDO;
import com.mawuya.core.dataobject.CategoryDO;
import com.mawuya.core.dataobject.ReviewDO;
import com.mawuya.core.dataobject.TagDO;
import com.mawuya.core.mapper.ArticleInfoMapper;
import com.mawuya.core.mapper.CategoryMapper;
import com.mawuya.core.mapper.ReviewInfoMapper;
import com.mawuya.core.mapper.TagMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 高质量种子文章 + 标签 + 评论 一次性加载器。
 *
 * <p>运行方式：</p>
 * <pre>
 *   mvn -pl mawuya-bms -Dtest=RichContentSeedLoader -DMAWUYA_SEED=run test
 * </pre>
 *
 * <p>未传 {@code MAWUYA_SEED=run} 时本类直接跳过（不污染常规 e2e 测试）。</p>
 *
 * <p>本次改造点：</p>
 * <ul>
 *   <li>清理所有历史 [SEED] / [RICH] 前缀的旧测试文章</li>
 *   <li>18 篇新文章覆盖 技术 / 生活 / 读书 / 随笔 4 大分类</li>
 *   <li>summary 加长到 60-100 字（约 1-2 行），前台卡片展示更饱满</li>
 *   <li>标题去除 [RICH] 前缀，改成可读性强的自然标题</li>
 * </ul>
 *
 * @author 钟启辉
 */
@SpringBootTest
@DisplayName("RichContentSeedLoader：高质量种子文章（按需运行）")
class RichContentSeedLoader {

    @Autowired private ArticleInfoMapper articleMapper;
    @Autowired private CategoryMapper    categoryMapper;
    @Autowired private TagMapper         tagMapper;
    @Autowired private ReviewInfoMapper  reviewMapper;

    @Test
    @DisplayName("插入若干高质量种子文章")
    void seed() {
        if (!"run".equalsIgnoreCase(System.getProperty("MAWUYA_SEED"))) {
            return;
        }
        cleanLegacyTestArticles();

        Map<String, Integer> catIds = ensureCategories();
        Map<String, Integer> tagIds = ensureTags();

        // ===== 技术（9 篇） =====================================================

        seedArticleWithTags(
                "分布式共识算法：Paxos / Raft / ZAB 的取舍坐标系",
                "在不确定的网络里达成确定的共识，是分布式系统最难的问题。本文从 FLP 不可能定理出发，把三大算法放进同一坐标系，说清它们各自的 trade-off。",
                MarkdownContents.DISTRIBUTED_CONSENSUS,
                catIds.get("技术"), 3460, 38, 245, 5,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&q=80",
                "2026-04-21 15:00:00",
                tagsOf(tagIds, "架构", "Java"));

        seedArticleWithTags(
                "Goroutine 不是免费的：写出真正高吞吐 Go 服务的 5 条军规",
                "Go 的协程便宜得让人误以为它是魔法。理解 GMP 模型与开销曲线，才能在生产里规避 goroutine 泄漏、滥用 sync.Pool 等典型陷阱。",
                MarkdownContents.GO_GOROUTINE,
                catIds.get("技术"), 2890, 31, 198, 4,
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=1200&q=80",
                "2026-03-15 11:20:00",
                tagsOf(tagIds, "Go", "架构"));

        seedArticleWithTags(
                "JVM G1 调优实战：把 P99 延迟从 1.2s 砍到 80ms 🚀",
                "线上推荐服务的真实事故复盘。Region / Mixed GC / IHOP 三个核心概念讲透，配上一份直接可用的 JVM 启动参数，性能调优不再是玄学。",
                MarkdownContents.JVM_G1,
                catIds.get("技术"), 4120, 52, 312, 6,
                "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1200&q=80",
                "2026-02-10 14:00:00",
                tagsOf(tagIds, "JVM", "Java", "架构"));

        seedArticleWithTags(
                "Docker 镜像分层艺术：从 680MB 缩到 180MB 的全过程",
                "镜像每一条指令都是一层只读 layer，顺序、大小、可缓存性决定了 CI 是 30 秒还是 30 分钟。多阶段构建 + .dockerignore 是关键。",
                MarkdownContents.DOCKER_LAYERS,
                catIds.get("技术"), 1980, 22, 156, 3,
                "https://images.unsplash.com/photo-1605379399642-870262d3d051?w=1200&q=80",
                "2026-01-28 10:30:00",
                tagsOf(tagIds, "架构"));

        seedArticleWithTags(
                "Kubernetes 三大探针你真的会用吗？Liveness / Readiness / Startup 全解",
                "很多团队把探针配错也不自知，直到某次发布全军覆没。一份能直接抄的标准配置，配合每种探针的作用域与典型踩坑场景。",
                MarkdownContents.K8S_PROBE,
                catIds.get("技术"), 2240, 26, 178, 4,
                "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?w=1200&q=80",
                "2026-01-12 09:30:00",
                tagsOf(tagIds, "架构"));

        seedArticleWithTags(
                "提示词工程：把 LLM 当作一名「实习生」来管理",
                "用 GPT 一年的最大感受：模型没变笨，是你不会问。整理 7 个被反复验证的 Prompt 模式，每个都附带可复制的模板，覆盖角色设定、思维链到 Function Calling。",
                MarkdownContents.AI_PROMPT_ENG,
                catIds.get("技术"), 5630, 71, 412, 8,
                "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=1200&q=80",
                "2026-05-08 17:30:00",
                tagsOf(tagIds, "AI", "效率"));

        seedArticleWithTags(
                "为什么 Vite 比 Webpack 快这么多？Native ESM 是真正的杀手锏",
                "Webpack 启动 30 秒，Vite 1 秒。秘密在被低估的浏览器特性 Native ESM 上。配合 esbuild 的依赖预构建与按需编译，前端构建工具迎来世代交替。",
                MarkdownContents.FRONTEND_VITE,
                catIds.get("技术"), 1740, 19, 132, 2,
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=1200&q=80",
                "2026-03-02 14:45:00",
                tagsOf(tagIds, "前端", "入门"));

        seedArticleWithTags(
                "OWASP Top 10：每个后端工程师都该背下来的安全清单",
                "工具检测不出业务逻辑漏洞，那才是真正会让公司上头条的东西。从 SQL 注入到 SSRF 全部过一遍，最后给团队一份 5 条最低红线，照做即可。",
                MarkdownContents.SECURITY_OWASP,
                catIds.get("技术"), 2570, 28, 192, 3,
                "https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=1200&q=80",
                "2026-02-25 16:00:00",
                tagsOf(tagIds, "架构", "Java"));

        seedArticleWithTags(
                "监控不等于可观测性：Metrics、Logs、Traces 三大支柱实战",
                "Monitoring 在问系统坏没坏，Observability 在问为什么坏。Google SRE 的黄金 4 信号 + 一份生产可用的 PromQL，让你真正打开线上系统的盖子。",
                MarkdownContents.OBSERVABILITY,
                catIds.get("技术"), 2090, 24, 165, 3,
                "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=1200&q=80",
                "2026-04-05 13:20:00",
                tagsOf(tagIds, "架构"));

        // ===== 生活（4 篇） =====================================================

        seedArticleWithTags(
                "京都的清晨：从二条城到伏见稻荷的 4 天步行手记",
                "京都最美的时刻不是樱花季的午后，而是清晨四点的清水寺。一份关于「慢，是一种能力」的旅行笔记，记下了二条城的黄莺地板与千本鸟居黄昏时燃烧的红色。",
                MarkdownContents.KYOTO_TRAVEL,
                catIds.get("生活"), 3210, 36, 268, 4,
                "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=1200&q=80;https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=1200&q=80",
                "2025-11-18 21:00:00",
                tagsOf(tagIds, "旅行", "生活", "摄影"));

        seedArticleWithTags(
                "30 分钟做一锅治愈系番茄牛腩，附三个能让味道翻倍的小技巧",
                "下班的晚上煮一锅番茄牛腩，比刷半小时短视频治愈得多。从备料到出锅的全程图文记录，三个简单到容易被忽略的细节，决定了它是「还行」还是「想再来一碗」。",
                MarkdownContents.FOOD_TOMATO_BEEF,
                catIds.get("生活"), 1450, 18, 124, 2,
                "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=1200&q=80",
                "2026-02-14 19:00:00",
                tagsOf(tagIds, "美食", "生活"));

        seedArticleWithTags(
                "跑步 365 天：从 5 公里跑不动到完成全马的 7 条复盘",
                "从静息心率 78 到 54、累计 2310 公里，跑步真正治愈的不是身体而是焦虑。没有惊人天赋，唯一做对的事情是没停下来，剩下的都是慢慢学到的纪律。",
                MarkdownContents.RUNNING_365,
                catIds.get("生活"), 1880, 22, 156, 1,
                "https://images.unsplash.com/photo-1552674605-db6ffd4facb5?w=1200&q=80",
                "2026-01-25 08:30:00",
                tagsOf(tagIds, "运动", "健康", "生活"));

        seedArticleWithTags(
                "让一天慢下来：三个能立刻照搬的反内卷小练习",
                "陷入「行动力崇拜」之后，我在某个周日下午看着银杏发呆四十分钟，才想起慢也是一种生产力。三个朴素到不像方法论的练习，把生活还给生活。",
                MarkdownContents.SLOW_LIFE,
                catIds.get("生活"), 1620, 21, 142, 2,
                "https://images.unsplash.com/photo-1490806843957-31f4c9a91c65?w=1200&q=80",
                "2026-04-19 21:45:00",
                tagsOf(tagIds, "生活", "效率", "哲思"));

        // ===== 读书（2 篇） =====================================================

        seedArticleWithTags(
                "《Designing Data-Intensive Applications》读书笔记 · 上",
                "DDIA 不教你某个数据库怎么用，而是讲清所有数据系统的底层物理规律——这些规律比任何具体技术活得都久。Replication / Transactions / Consensus 三章梳理。",
                MarkdownContents.DDIA_NOTES,
                catIds.get("读书"), 1320, 16, 108, 1,
                "https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=1200&q=80",
                "2026-03-30 22:15:00",
                tagsOf(tagIds, "读书", "架构"));

        seedArticleWithTags(
                "《Deep Work》读后感：在协作时代主动选择孤独",
                "Cal Newport 的反潮流主张：深度工作的能力会越来越稀缺也越来越值钱。四条建议加一个让我改变最大的练习——每周日晚上手写下周深度工作时段。",
                MarkdownContents.DEEP_WORK,
                catIds.get("读书"), 1710, 24, 168, 2,
                "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=1200&q=80",
                "2026-02-08 22:30:00",
                tagsOf(tagIds, "读书", "效率", "哲思"));

        // ===== 随笔（3 篇） =====================================================

        seedArticleWithTags(
                "关于焦虑：一份给程序员的 30 天小练习",
                "睡不着、心慌、对什么都提不起兴趣——你试过的方法我大多也试过。下面这些是真正帮到我的，朴素到不像方法论。如果你已经尽力，请去看医生，那不是软弱。",
                MarkdownContents.ANXIETY_30,
                catIds.get("随笔"), 2430, 35, 286, 0,
                "https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=1200&q=80",
                "2026-04-22 17:00:00",
                tagsOf(tagIds, "随笔", "健康", "哲思"));

        seedArticleWithTags(
                "三年读了 87 本书：一份朴素到能立刻照搬的阅读系统",
                "深度来自重复，宽度来自跨界。三个工具、三个习惯、三行读后感——阅读的目的不是「读了多少本」，而是「读完之后我变成了一个稍微不一样的人」。",
                MarkdownContents.READING_HABITS,
                catIds.get("随笔"), 1290, 17, 134, 1,
                "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=1200&q=80",
                "2026-03-22 20:00:00",
                tagsOf(tagIds, "读书", "效率", "随笔"));

        seedArticleWithTags(
                "夜里十一点的便利店，是城市最诚实的样子",
                "白天每个人都在演自己希望成为的人，到了晚上微波炉「叮」的一声响起时，才看到大家原本的样子。城市的功能性是白天的事，夜晚才是它真正的容器。",
                MarkdownContents.CITY_NIGHT,
                catIds.get("随笔"), 980, 14, 112, 0,
                "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200&q=80",
                "2026-05-04 23:30:00",
                tagsOf(tagIds, "随笔", "哲思", "生活"));

        // 评论
        seedReviews();

        System.out.println("== RichContentSeedLoader DONE ==");
    }

    // ----------------------------------------------------------------- helpers

    /** 清理所有历史 [SEED] / [RICH] 前缀的旧文章 + 关联标签 + 评论 */
    private void cleanLegacyTestArticles() {
        List<ArticleDO> all = articleMapper.selectAllNoContent(new HashMap<>());
        List<Integer> snList = all.stream()
                .filter(a -> {
                    String t = a.getArticleTitle();
                    return t != null && (t.startsWith("[SEED]") || t.startsWith("[RICH]"));
                })
                .map(ArticleDO::getSn)
                .collect(Collectors.toList());
        System.out.println("[seed] clean legacy articles count = " + snList.size());
        for (Integer sn : snList) {
            tagMapper.unbindByArticle(sn);
            for (ReviewDO r : reviewMapper.selectByArticleSn(sn)) {
                reviewMapper.deleteById(r.getSn());
            }
            articleMapper.deleteById(sn);
        }
    }

    private Map<String, Integer> ensureCategories() {
        String[] names = {"技术", "生活", "随笔", "读书", "未分类"};
        Map<String, Integer> map = new LinkedHashMap<>();
        for (CategoryDO c : categoryMapper.selectList(new HashMap<>())) {
            map.put(c.getCategoryName(), c.getSn());
        }
        for (String name : names) {
            if (!map.containsKey(name)) {
                categoryMapper.insert(new CategoryDO(name));
                CategoryDO c = categoryMapper.selectList(new HashMap<>()).stream()
                        .filter(x -> name.equals(x.getCategoryName()))
                        .findFirst().orElseThrow(IllegalStateException::new);
                map.put(name, c.getSn());
            }
        }
        return map;
    }

    private Map<String, Integer> ensureTags() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (TagDO t : tagMapper.selectList(new HashMap<>())) {
            map.put(t.getTagName(), t.getSn());
        }
        return map;
    }

    private List<Integer> tagsOf(Map<String, Integer> tagIds, String... names) {
        return Arrays.stream(names)
                .map(tagIds::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void seedArticleWithTags(String title, String summary, String content,
                                     Integer categorySn, int readNum, int reviewNum,
                                     int praiseNum, int teaseNum,
                                     String pictureUrl, String insertTime,
                                     List<Integer> tagSns) {

        // 防止重复运行同一份种子时 title 冲突，先清掉同名旧文
        ArticleDO dup = articleMapper.selectAllNoContent(new HashMap<>()).stream()
                .filter(x -> title.equals(x.getArticleTitle()))
                .findFirst().orElse(null);
        if (dup != null) {
            tagMapper.unbindByArticle(dup.getSn());
            for (ReviewDO r : reviewMapper.selectByArticleSn(dup.getSn())) {
                reviewMapper.deleteById(r.getSn());
            }
            articleMapper.deleteById(dup.getSn());
        }

        ArticleDO a = new ArticleDO()
                .setCategorySn(categorySn == null ? 0 : categorySn)
                .setReadNum(readNum)
                .setReviewNum(reviewNum)
                .setPraiseNum(praiseNum)
                .setTeaseNum(teaseNum)
                .setPictureUrl(pictureUrl)
                .setArticleTitle(title)
                .setArticleSummary(summary)
                .setArticleContent(content)
                .setInsertTime(insertTime)
                .setUpdateTime(insertTime);
        articleMapper.insert(a);

        // 通过 title 反查 sn 后修正 insertTime（INSERT SQL 用了 NOW()，这里强制覆盖）
        ArticleDO saved = articleMapper.selectAllNoContent(new HashMap<>()).stream()
                .filter(x -> title.equals(x.getArticleTitle()))
                .findFirst().orElseThrow(IllegalStateException::new);

        saved.setArticleContent(content)
                .setInsertTime(insertTime)
                .setUpdateTime(insertTime)
                .setReadNum(readNum)
                .setReviewNum(reviewNum)
                .setPraiseNum(praiseNum)
                .setTeaseNum(teaseNum)
                .setPictureUrl(pictureUrl)
                .setArticleTitle(title)
                .setArticleSummary(summary)
                .setCategorySn(categorySn == null ? 0 : categorySn);
        articleMapper.update(saved);

        for (Integer tagSn : tagSns) {
            tagMapper.bindArticleTag(saved.getSn(), tagSn);
        }
    }

    /** 给热门文章追加一些评论，让前台「最新评论」widget 有真实数据展示 */
    private void seedReviews() {
        List<ArticleDO> arts = articleMapper.selectAllNoContent(new HashMap<>()).stream()
                .sorted((x, y) -> Integer.compare(y.getReadNum(), x.getReadNum()))
                .limit(6)
                .collect(Collectors.toList());

        String[][] sample = {
                {"陈Lin",    "结构清晰，干货密度高。CoT 那段确实是我以前一直忽略的，受教。"},
                {"路过的鱼", "图很清晰，配色舒服 👍 立刻收藏起来下午细看。"},
                {"小白丙",   "刚入行不久，看完受益良多，谢谢分享，期待下篇。"},
                {"老 K",     "建议补充一下版本号与压测数据，方便对照。整体质量很高。"},
                {"匿名读者", "排版很赞，作者很用心 🌸 默默点赞。"},
                {"Tony.W",   "关于 trade-off 那一节写得最好，工程没有银弹这句话我贴墙上了。"}
        };

        for (ArticleDO a : arts) {
            int n = 2 + Math.floorMod(a.getSn(), 3); // 2~4 条
            for (int i = 0; i < n && i < sample.length; i++) {
                ReviewDO r = new ReviewDO()
                        .setArticleSn(a.getSn())
                        .setPsn(0)
                        .setReviewName(sample[i][0])
                        .setReviewContent(sample[i][1]);
                reviewMapper.insert(r);
            }
            reviewMapper.refreshArticleReviewNum(a.getSn());
        }
    }
}
