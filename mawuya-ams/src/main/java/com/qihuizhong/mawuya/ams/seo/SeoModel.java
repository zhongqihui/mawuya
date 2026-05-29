/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.ams.seo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 单页 SEO 元数据模型，注入到 Thymeleaf model 中作 {@code seo}。
 *
 * <p>视图层只依赖此对象暴露的 getter，Controller 通过 {@link #of} 系列工厂方法快速构造，
 * 也可由 {@link SeoModelAdvice} 在请求处理后兜底填充缺失字段。</p>
 *
 * @author 钟启辉
 */
public class SeoModel {

    /** 页面 title（不带站名后缀，由模板拼接） */
    private String title;
    /** meta description（建议 70-160 字符） */
    private String description;
    /** meta keywords（逗号分隔） */
    private String keywords;
    /** canonical 完整 URL（含 scheme + host + path，无 query；列表页可带 page=N） */
    private String canonical;
    /** OG / Twitter 缩略图绝对地址 */
    private String image;
    /** OG type：website / article / profile */
    private String ogType;
    /** 是否允许 noindex（true 表示禁止索引，例如搜索结果空页、参数页） */
    private boolean noindex;
    /** 文章页特有：发布时间 ISO8601 */
    private String articlePublishedTime;
    /** 文章页特有：修改时间 ISO8601 */
    private String articleModifiedTime;
    /** 文章页特有：作者名 */
    private String articleAuthor;
    /** 文章页特有：分类 / 标签（充当 og:article:section / tag） */
    private String articleSection;
    private List<String> articleTags = new ArrayList<>();
    /** 面包屑：保持插入顺序，key=文字，value=URL（最后一项 URL 可为 null 表示当前页） */
    private Map<String, String> breadcrumbs = new LinkedHashMap<>();
    /** 额外 JSON-LD（已序列化为 JSON 字符串），可叠加多块 */
    private List<String> jsonLdBlocks = new ArrayList<>();

    public static SeoModel of(String title, String description) {
        SeoModel m = new SeoModel();
        m.title = title;
        m.description = description;
        m.ogType = "website";
        return m;
    }

    public SeoModel addBreadcrumb(String name, String url) {
        this.breadcrumbs.put(name, url);
        return this;
    }

    public SeoModel addJsonLd(String json) {
        if (json != null && !json.isEmpty()) {
            this.jsonLdBlocks.add(json);
        }
        return this;
    }

    public String getTitle() { return title; }
    public SeoModel setTitle(String title) { this.title = title; return this; }

    public String getDescription() { return description; }
    public SeoModel setDescription(String description) { this.description = description; return this; }

    public String getKeywords() { return keywords; }
    public SeoModel setKeywords(String keywords) { this.keywords = keywords; return this; }

    public String getCanonical() { return canonical; }
    public SeoModel setCanonical(String canonical) { this.canonical = canonical; return this; }

    public String getImage() { return image; }
    public SeoModel setImage(String image) { this.image = image; return this; }

    public String getOgType() { return ogType; }
    public SeoModel setOgType(String ogType) { this.ogType = ogType; return this; }

    public boolean isNoindex() { return noindex; }
    public SeoModel setNoindex(boolean noindex) { this.noindex = noindex; return this; }

    public String getArticlePublishedTime() { return articlePublishedTime; }
    public SeoModel setArticlePublishedTime(String t) { this.articlePublishedTime = t; return this; }

    public String getArticleModifiedTime() { return articleModifiedTime; }
    public SeoModel setArticleModifiedTime(String t) { this.articleModifiedTime = t; return this; }

    public String getArticleAuthor() { return articleAuthor; }
    public SeoModel setArticleAuthor(String articleAuthor) { this.articleAuthor = articleAuthor; return this; }

    public String getArticleSection() { return articleSection; }
    public SeoModel setArticleSection(String articleSection) { this.articleSection = articleSection; return this; }

    public List<String> getArticleTags() { return articleTags; }
    public SeoModel setArticleTags(List<String> articleTags) {
        this.articleTags = articleTags == null ? new ArrayList<>() : articleTags;
        return this;
    }

    public Map<String, String> getBreadcrumbs() { return breadcrumbs; }
    public SeoModel setBreadcrumbs(Map<String, String> breadcrumbs) {
        this.breadcrumbs = breadcrumbs == null ? new LinkedHashMap<>() : breadcrumbs;
        return this;
    }

    public List<String> getJsonLdBlocks() { return jsonLdBlocks; }
    public SeoModel setJsonLdBlocks(List<String> jsonLdBlocks) {
        this.jsonLdBlocks = jsonLdBlocks == null ? new ArrayList<>() : jsonLdBlocks;
        return this;
    }
}
