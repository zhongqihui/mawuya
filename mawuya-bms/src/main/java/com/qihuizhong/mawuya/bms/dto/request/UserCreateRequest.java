/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户创建请求 DTO（{@code POST /bms/api/user/create}）。
 *
 * @author 钟启辉
 */
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_]{3,32}$", message = "用户名仅允许字母数字下划线，长度 3-32")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6-64")
    private String password;

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱过长")
    private String email;

    /** 1=启用 0=禁用，默认 1 */
    private Integer enabled = 1;

    /** 角色 codes，逗号分隔（前端 form 表单常用），允许为空 */
    private String roleCodes;

    public String getUsername() { return username; }
    public UserCreateRequest setUsername(String username) { this.username = username; return this; }

    public String getPassword() { return password; }
    public UserCreateRequest setPassword(String password) { this.password = password; return this; }

    public String getNickname() { return nickname; }
    public UserCreateRequest setNickname(String nickname) { this.nickname = nickname; return this; }

    public String getEmail() { return email; }
    public UserCreateRequest setEmail(String email) { this.email = email; return this; }

    public Integer getEnabled() { return enabled; }
    public UserCreateRequest setEnabled(Integer enabled) { this.enabled = enabled; return this; }

    public String getRoleCodes() { return roleCodes; }
    public UserCreateRequest setRoleCodes(String roleCodes) { this.roleCodes = roleCodes; return this; }
}
