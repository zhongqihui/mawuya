/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 标签新建请求 DTO（{@code POST /bms/api/tag/create}）。
 *
 * @author 钟启辉
 */
public class TagCreateRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 30, message = "标签名称最长 30 字符")
    private String tagName;

    /**
     * 标签颜色（HEX）：可空 → 服务端使用默认色。
     * 仅接受 {@code #rgb} 或 {@code #rrggbb}，避免 CSS 注入（inline style 渲染）。
     */
    @Pattern(regexp = "^$|^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$",
            message = "颜色格式不合法，仅支持 #rgb 或 #rrggbb")
    @Size(max = 20, message = "颜色字符串过长")
    private String tagColor;

    public String getTagName() { return tagName; }
    public TagCreateRequest setTagName(String tagName) { this.tagName = tagName; return this; }

    public String getTagColor() { return tagColor; }
    public TagCreateRequest setTagColor(String tagColor) { this.tagColor = tagColor; return this; }
}
