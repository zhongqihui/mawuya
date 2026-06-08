/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.ams.seo;

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
 * <p>遵循阿里 POJO 规约：字段不在声明处赋值，集合在构造方法中初始化（视图渲染期空集合避免 NPE）。</p>
 *
 * @author 钟启辉
 */
public class SeoModel {

    /** 默认面包屑容量（首页/分类/详情通常 ≤ 4 项） */
    private static final int DEFAULT_BREADCRUMB_CAPACITY = 4;
    /** 默认标签 / JSON-LD 块容量 */
    private static final int DEFAULT_LIST_CAPACITY = 4;

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
    private Boolean noindex;
    /** 文章页特有：发布时间 ISO8601 */
    private String articlePublishedTime;
    /** 文章页特有：修改时间 ISO8601 */
    private String articleModifiedTime;
    /** 文章页特有：作者名 */
    private String articleAuthor;
    /** 文章页特有：分类 / 标签（充当 og:article:section / tag） */
    private String articleSection;
    /** 文章页特有：标签名列表 */
    private List<String> articleTags;
    /** 面包屑：保持插入顺序，key=文字，value=URL（最后一项 URL 可为 null 表示当前页） */
    private Map<String, String> breadcrumbs;
    /** 额外 JSON-LD（已序列化为 JSON 字符串），可叠加多块 */
    private List<String> jsonLdBlocks;

    public SeoModel() {
        // 集合在构造时一次性初始化，避免视图渲染期 NPE
        this.articleTags = new ArrayList<>(DEFAULT_LIST_CAPACITY);
        this.breadcrumbs = new LinkedHashMap<>(DEFAULT_BREADCRUMB_CAPACITY);
        this.jsonLdBlocks = new ArrayList<>(DEFAULT_LIST_CAPACITY);
        this.noindex = Boolean.FALSE;
    }

    public static SeoModel of(String title, String description) {
        SeoModel m = new SeoModel();
        m.title = title;
        m.description = description;
        m.ogType = "website";
        return m;
    }

    /**
     * 追加一项面包屑节点。
     *
     * @param name 显示文本（如"首页"、"分类"、当前页标题）
     * @param url  跳转地址。**约定使用站内绝对路径**（如 {@code "/"}、{@code "/categories"}），
     *             不要传 {@code seoProperties.getSiteUrl() + "/xxx"} 这样的绝对 URL：
     *             否则在 localhost 访问时会跳到生产域名，破坏本机/灰度调试。
     *             浏览器会基于当前 host 解析相对路径，跨环境天然兼容。
     *             末项（当前页）传 {@code null} 即可，模板会渲染为不可点击的纯文本。
     *             BreadcrumbList JSON-LD 由 Controller 单独构造（item.id 才需要绝对 URL），
     *             与这里的 url 是两路独立数据，互不影响。
     */
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

    /**
     * 模板里允许写 {@code th:if="${seo.noindex}"} 直接判定，所以保留 isXxx 形式的访问器。
     * Bean 规约下基本类型 boolean 才有 isXxx，包装 Boolean 则只能用 getXxx；为兼顾两种调用，
     * 同时提供 {@code isNoindex()} 与 {@code getNoindex()}。
     */
    public Boolean getNoindex() { return noindex; }
    public boolean isNoindex() { return Boolean.TRUE.equals(noindex); }
    public SeoModel setNoindex(Boolean noindex) { this.noindex = noindex; return this; }

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
        this.articleTags = articleTags == null ? new ArrayList<>(DEFAULT_LIST_CAPACITY) : articleTags;
        return this;
    }

    public Map<String, String> getBreadcrumbs() { return breadcrumbs; }
    public SeoModel setBreadcrumbs(Map<String, String> breadcrumbs) {
        this.breadcrumbs = breadcrumbs == null ? new LinkedHashMap<>(DEFAULT_BREADCRUMB_CAPACITY) : breadcrumbs;
        return this;
    }

    public List<String> getJsonLdBlocks() { return jsonLdBlocks; }
    public SeoModel setJsonLdBlocks(List<String> jsonLdBlocks) {
        this.jsonLdBlocks = jsonLdBlocks == null ? new ArrayList<>(DEFAULT_LIST_CAPACITY) : jsonLdBlocks;
        return this;
    }

    @Override
    public String toString() {
        return "SeoModel{title='" + title + "', canonical='" + canonical + "', ogType='" + ogType
                + "', noindex=" + noindex + '}';
    }
}
