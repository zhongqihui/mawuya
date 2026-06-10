/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 标签更新请求 DTO（{@code POST /bms/api/tag/update}）。
 *
 * @author 钟启辉
 */
public class TagUpdateRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 30, message = "标签名称最长 30 字符")
    private String tagName;

    @Pattern(regexp = "^$|^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$",
            message = "颜色格式不合法，仅支持 #rgb 或 #rrggbb")
    @Size(max = 20, message = "颜色字符串过长")
    private String tagColor;

    public Integer getSn() { return sn; }
    public TagUpdateRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getTagName() { return tagName; }
    public TagUpdateRequest setTagName(String tagName) { this.tagName = tagName; return this; }

    public String getTagColor() { return tagColor; }
    public TagUpdateRequest setTagColor(String tagColor) { this.tagColor = tagColor; return this; }
}
