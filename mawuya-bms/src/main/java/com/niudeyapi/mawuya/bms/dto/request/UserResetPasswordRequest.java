/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户密码重置请求 DTO（{@code POST /bms/api/user/reset}）。
 *
 * @author 钟启辉
 */
public class UserResetPasswordRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6-64")
    private String password;

    public Integer getSn() { return sn; }
    public UserResetPasswordRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getPassword() { return password; }
    public UserResetPasswordRequest setPassword(String password) { this.password = password; return this; }
}
