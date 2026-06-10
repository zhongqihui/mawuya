/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 文章「保存为草稿」请求 DTO（{@code POST /bms/api/article/saveDraft}）。
 *
 * <p>与 {@link ArticleCreateRequest} 的主要区别：
 * <ul>
 *   <li>支持 {@code sn}：传 null 视为新建草稿，传非空视为更新已有草稿；</li>
 *   <li>{@code articleContent} 允许为空——草稿允许只填了标题就先保存。</li>
 * </ul>
 *
 * @author 钟启辉
 */
public class ArticleDraftRequest {

    /** 草稿 sn：为空表示新建；非空表示更新现有草稿 */
    private Integer sn;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长 200 字符")
    private String articleTitle;

    /** 分类 sn（0 表示未分类，可空） */
    private Integer categorySn;

    @Size(max = 1000, message = "摘要最长 1000 字符")
    private String articleSummary;

    /** 草稿场景：正文允许为空 */
    private String articleContent;

    public Integer getSn() { return sn; }
    public ArticleDraftRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getArticleTitle() { return articleTitle; }
    public ArticleDraftRequest setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; return this; }

    public Integer getCategorySn() { return categorySn; }
    public ArticleDraftRequest setCategorySn(Integer categorySn) { this.categorySn = categorySn; return this; }

    public String getArticleSummary() { return articleSummary; }
    public ArticleDraftRequest setArticleSummary(String articleSummary) { this.articleSummary = articleSummary; return this; }

    public String getArticleContent() { return articleContent; }
    public ArticleDraftRequest setArticleContent(String articleContent) { this.articleContent = articleContent; return this; }
}
