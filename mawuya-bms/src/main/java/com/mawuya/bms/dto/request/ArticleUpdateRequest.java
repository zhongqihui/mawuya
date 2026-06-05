/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 文章更新请求 DTO（{@code POST /bms/api/article/update}）。
 *
 * @author 钟启辉
 */
public class ArticleUpdateRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长 200 字符")
    private String articleTitle;

    private Integer categorySn;

    @Size(max = 1000, message = "摘要最长 1000 字符")
    private String articleSummary;

    @NotBlank(message = "正文不能为空")
    private String articleContent;

    public Integer getSn() { return sn; }
    public ArticleUpdateRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getArticleTitle() { return articleTitle; }
    public ArticleUpdateRequest setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; return this; }

    public Integer getCategorySn() { return categorySn; }
    public ArticleUpdateRequest setCategorySn(Integer categorySn) { this.categorySn = categorySn; return this; }

    public String getArticleSummary() { return articleSummary; }
    public ArticleUpdateRequest setArticleSummary(String articleSummary) { this.articleSummary = articleSummary; return this; }

    public String getArticleContent() { return articleContent; }
    public ArticleUpdateRequest setArticleContent(String articleContent) { this.articleContent = articleContent; return this; }
}
