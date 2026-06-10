/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * 文章批量绑定标签请求 DTO（{@code POST /bms/api/tag/bindArticle}）。
 *
 * <p>语义：先全清掉该文章的所有标签关联，再按 {@code existingTagSns + newTagNames} 重新绑定。
 * 当两个列表都为空时等价于「清空该文章所有标签」。</p>
 *
 * @author 钟启辉
 */
public class ArticleTagBindRequest {

    @NotNull(message = "articleSn 不能为空")
    private Integer articleSn;

    /** 现有标签 sn 列表（可空） */
    private List<Integer> existingTagSns;

    /**
     * 新建标签名列表（可空）：
     * 由前端 select2 tags 模式允许用户现场输入未列出的标签名，提交时由 service 自动创建。
     */
    private List<String> newTagNames;

    public Integer getArticleSn() { return articleSn; }
    public ArticleTagBindRequest setArticleSn(Integer articleSn) { this.articleSn = articleSn; return this; }

    public List<Integer> getExistingTagSns() { return existingTagSns; }
    public ArticleTagBindRequest setExistingTagSns(List<Integer> existingTagSns) { this.existingTagSns = existingTagSns; return this; }

    public List<String> getNewTagNames() { return newTagNames; }
    public ArticleTagBindRequest setNewTagNames(List<String> newTagNames) { this.newTagNames = newTagNames; return this; }
}
