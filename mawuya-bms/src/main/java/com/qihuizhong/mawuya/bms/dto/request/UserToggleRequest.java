/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import javax.validation.constraints.NotNull;

/**
 * 用户启停请求 DTO（{@code POST /bms/api/user/toggle}）。
 *
 * @author 钟启辉
 */
public class UserToggleRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @NotNull(message = "enabled 不能为空")
    private Integer enabled;

    public Integer getSn() { return sn; }
    public UserToggleRequest setSn(Integer sn) { this.sn = sn; return this; }

    public Integer getEnabled() { return enabled; }
    public UserToggleRequest setEnabled(Integer enabled) { this.enabled = enabled; return this; }
}
