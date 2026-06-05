/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 用户创建响应 DTO（仅包含主键 sn，前端用以引导后续操作）。
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserCreateResponse {

    private Integer sn;

    public UserCreateResponse() {}

    public UserCreateResponse(Integer sn) {
        this.sn = sn;
    }

    public Integer getSn() { return sn; }
    public UserCreateResponse setSn(Integer sn) { this.sn = sn; return this; }
}
