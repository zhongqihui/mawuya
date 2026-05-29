/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.controller;

import com.qihuizhong.mawuya.ams.seo.SeoProperties;
import com.qihuizhong.mawuya.ams.seo.SeoUtils;
import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.entity.Category;
import com.qihuizhong.mawuya.core.entity.Tag;
import com.qihuizhong.mawuya.core.service.ArticleService;
import com.qihuizhong.mawuya.core.service.CategoryService;
import com.qihuizhong.mawuya.core.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

/**
 * SEO 动态资源出口：
 * <ul>
 *   <li>{@code GET /sitemap.xml} – 由文章 / 分类 / 标签 / 静态页动态生成</li>
 *   <li>{@code GET /robots.txt} – 输出抓取策略，引用 sitemap</li>
 *   <li>{@code GET /rss.xml} – 提供 RSS 2.0 订阅源（提升搜索引擎抓取频次）</li>
 * </ul>
 *
 * <p>所有内容直接 String 返回，避免引入额外模板；遵守 sitemap.org 0.9 / RSS 2.0 规范。</p>
 *
 * @author 钟启辉
 */
@Controller
public class SeoController {

    private static final int RSS_LIMIT = 20;

    private final SeoProperties seo;
    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @Autowired
    public SeoController(SeoProperties seo,
                         ArticleService articleService,
                         CategoryService categoryService,
                         TagService tagService) {
        this.seo = seo;
        this.articleService = articleService;
        this.categoryService = categoryService;
        this.tagService = tagService;
    }

    /**
     * 动态 sitemap.xml：包含首页、归档、分类列表、标签列表、所有分类详情、所有标签详情、所有文章详情。
     * 文章 lastmod 优先 update_time，回退 insert_time；priority 按页面类型分配。
     */
    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        String base = seo.getSiteUrl();
        StringBuilder sb = new StringBuilder(8192);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // 1) 首页：每天更新一次，权重最高
        appendUrl(sb, base + "/", null, "daily", "1.0");
        // 2) 归档 / 分类列表 / 标签列表 / 关于
        appendUrl(sb, base + "/archive", null, "weekly", "0.8");
        appendUrl(sb, base + "/categories", null, "weekly", "0.7");
        appendUrl(sb, base + "/tags", null, "weekly", "0.7");
        appendUrl(sb, base + "/about", null, "monthly", "0.5");

        int max = Math.max(seo.getSitemapMaxUrls(), 100);
        int count = 5;

        // 3) 所有文章
        List<ArticleInfo> articles = articleService.getAllNoContent(new HashMap<>());
        if (articles != null) {
            for (ArticleInfo a : articles) {
                if (count++ >= max) break;
                String last = a.getUpdateTime() != null ? a.getUpdateTime() : a.getInsertTime();
                appendUrl(sb, base + "/" + a.getSn(), SeoUtils.toIso8601(last), "monthly", "0.9");
            }
        }

        // 4) 分类详情
        List<Category> cats = categoryService.selectList(new HashMap<>());
        if (cats != null) {
            for (Category c : cats) {
                if (count++ >= max) break;
                appendUrl(sb, base + "/categories/" + c.getSn(), null, "weekly", "0.6");
            }
        }

        // 5) 标签详情
        List<Tag> tags = tagService.getCloud();
        if (tags != null) {
            for (Tag t : tags) {
                if (count++ >= max) break;
                appendUrl(sb, base + "/tags/" + t.getSn(), null, "weekly", "0.6");
            }
        }

        sb.append("</urlset>\n");
        return sb.toString();
    }

    /**
     * 动态 robots.txt：
     * - 生产环境（mawuya.seo.robots-index=true）放开抓取，禁止索引参数页 / 内部接口；
     * - 预发 / 测试环境关闭抓取（{@code Disallow: /}）。
     * - 末尾输出 Sitemap 绝对地址，方便搜索引擎自动发现。
     */
    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robots() {
        StringBuilder sb = new StringBuilder(256);
        if (!seo.isRobotsIndex()) {
            sb.append("# noindex environment\n");
            sb.append("User-agent: *\n");
            sb.append("Disallow: /\n");
            return sb.toString();
        }
        sb.append("User-agent: *\n");
        // 屏蔽搜索结果页（无内容增益、易被判 thin content）、错误页、上传原图（避免占抓取预算）
        sb.append("Disallow: /search\n");
        sb.append("Disallow: /comments/submit\n");
        sb.append("Disallow: /error\n");
        sb.append("Disallow: /upload/\n");
        // 静态资源允许（默认即允许，显式声明利于排查）
        sb.append("Allow: /statics/\n");
        sb.append("\n");
        sb.append("Sitemap: ").append(seo.getSiteUrl()).append("/sitemap.xml\n");
        return sb.toString();
    }

    /**
     * RSS 2.0 订阅源：最近 {@value RSS_LIMIT} 篇文章，能显著提升新文章被搜索引擎抓取的速度。
     */
    @GetMapping(value = "/rss.xml", produces = "application/rss+xml; charset=UTF-8")
    @ResponseBody
    public String rss() {
        String base = seo.getSiteUrl();
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        sb.append("<channel>\n");
        sb.append("<title>").append(SeoUtils.escapeAttr(seo.getSiteName())).append("</title>\n");
        sb.append("<link>").append(base).append("/</link>\n");
        sb.append("<description>")
                .append(SeoUtils.escapeAttr(seo.getDefaultDescription()))
                .append("</description>\n");
        sb.append("<language>zh-cn</language>\n");
        sb.append("<atom:link href=\"").append(base).append("/rss.xml\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        List<ArticleInfo> articles = articleService.getAllNoContent(new HashMap<>());
        if (articles != null) {
            int n = 0;
            for (ArticleInfo a : articles) {
                if (n++ >= RSS_LIMIT) break;
                String url = base + "/" + a.getSn();
                sb.append("<item>\n");
                sb.append("<title>").append(SeoUtils.escapeAttr(a.getArticleTitle())).append("</title>\n");
                sb.append("<link>").append(url).append("</link>\n");
                sb.append("<guid isPermaLink=\"true\">").append(url).append("</guid>\n");
                sb.append("<pubDate>").append(SeoUtils.escapeAttr(SeoUtils.toIso8601(a.getInsertTime())))
                        .append("</pubDate>\n");
                sb.append("<description><![CDATA[")
                        .append(a.getArticleSummary() == null ? "" : a.getArticleSummary())
                        .append("]]></description>\n");
                sb.append("</item>\n");
            }
        }

        sb.append("</channel>\n");
        sb.append("</rss>\n");
        return sb.toString();
    }

    private static void appendUrl(StringBuilder sb, String loc, String lastmod, String changefreq, String priority) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(loc).append("</loc>\n");
        if (lastmod != null && !lastmod.isEmpty()) {
            sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        }
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }
}
