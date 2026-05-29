/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 分类新增请求 DTO（{@code POST /bms/api/category/create}）。
 *
 * @author 钟启辉
 */
public class CategoryCreateRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称最长 64 字符")
    private String categoryName;

    public String getCategoryName() { return categoryName; }
    public CategoryCreateRequest setCategoryName(String categoryName) { this.categoryName = categoryName; return this; }
}
