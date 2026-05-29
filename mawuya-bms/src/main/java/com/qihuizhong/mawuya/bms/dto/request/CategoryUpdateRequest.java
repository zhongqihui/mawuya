/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 分类更新请求 DTO（{@code POST /bms/api/category/update}）。
 *
 * @author 钟启辉
 */
public class CategoryUpdateRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称最长 64 字符")
    private String categoryName;

    public Integer getSn() { return sn; }
    public CategoryUpdateRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getCategoryName() { return categoryName; }
    public CategoryUpdateRequest setCategoryName(String categoryName) { this.categoryName = categoryName; return this; }
}
