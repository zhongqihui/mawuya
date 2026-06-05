/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * BMS 登录请求 DTO（{@code POST /bms/api/auth/login}）。
 *
 * @author 钟启辉
 */
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名过长")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 128, message = "密码长度不合法")
    private String password;

    public String getUsername() { return username; }
    public LoginRequest setUsername(String username) { this.username = username; return this; }

    public String getPassword() { return password; }
    public LoginRequest setPassword(String password) { this.password = password; return this; }
}
