/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 主题切换请求 DTO（{@code POST /bms/api/theme/apply}）。
 *
 * <p>code 必须由字母、数字、下划线、短横构成，长度 1~32，避免任意值落到 blog_info 表。</p>
 *
 * @author 钟启辉
 */
public class ThemeApplyRequest {

    @NotBlank(message = "主题 code 不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,32}$", message = "主题 code 格式不合法")
    private String code;

    public String getCode() { return code; }
    public ThemeApplyRequest setCode(String code) { this.code = code; return this; }
}
