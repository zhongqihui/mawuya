/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 角色简要响应 DTO（{@code GET /bms/api/user/roles} 返回 data.list 元素）。
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleItemResponse {

    private Integer sn;
    private String code;
    private String name;
    private String description;

    public Integer getSn() { return sn; }
    public RoleItemResponse setSn(Integer sn) { this.sn = sn; return this; }

    public String getCode() { return code; }
    public RoleItemResponse setCode(String code) { this.code = code; return this; }

    public String getName() { return name; }
    public RoleItemResponse setName(String name) { this.name = name; return this; }

    public String getDescription() { return description; }
    public RoleItemResponse setDescription(String description) { this.description = description; return this; }
}
