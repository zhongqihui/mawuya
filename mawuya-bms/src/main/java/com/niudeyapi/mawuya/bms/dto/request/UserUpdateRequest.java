/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户更新请求 DTO（{@code POST /bms/api/user/update}）。
 *
 * <p>所有字段除 sn 外均可空：null 表示不修改；roleCodes 空串表示清空角色。</p>
 *
 * @author 钟启辉
 */
public class UserUpdateRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱过长")
    private String email;

    /** 1=启用 0=禁用 null=不修改 */
    private Integer enabled;

    /** 角色 codes，逗号分隔。null=不修改；""=清空 */
    private String roleCodes;

    public Integer getSn() { return sn; }
    public UserUpdateRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getNickname() { return nickname; }
    public UserUpdateRequest setNickname(String nickname) { this.nickname = nickname; return this; }

    public String getEmail() { return email; }
    public UserUpdateRequest setEmail(String email) { this.email = email; return this; }

    public Integer getEnabled() { return enabled; }
    public UserUpdateRequest setEnabled(Integer enabled) { this.enabled = enabled; return this; }

    public String getRoleCodes() { return roleCodes; }
    public UserUpdateRequest setRoleCodes(String roleCodes) { this.roleCodes = roleCodes; return this; }
}
