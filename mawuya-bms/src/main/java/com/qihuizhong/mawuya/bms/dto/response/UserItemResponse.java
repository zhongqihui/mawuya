/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 用户简要信息 DTO（用户列表返回 data.list 元素）。
 *
 * <p>密码哈希永远不暴露给前端。</p>
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserItemResponse {

    private Integer sn;
    private String username;
    private String nickname;
    private String email;
    private Integer enabled;
    private List<String> roleCodes;
    private String createdAt;
    private String lastLoginAt;

    public Integer getSn() { return sn; }
    public UserItemResponse setSn(Integer sn) { this.sn = sn; return this; }

    public String getUsername() { return username; }
    public UserItemResponse setUsername(String username) { this.username = username; return this; }

    public String getNickname() { return nickname; }
    public UserItemResponse setNickname(String nickname) { this.nickname = nickname; return this; }

    public String getEmail() { return email; }
    public UserItemResponse setEmail(String email) { this.email = email; return this; }

    public Integer getEnabled() { return enabled; }
    public UserItemResponse setEnabled(Integer enabled) { this.enabled = enabled; return this; }

    public List<String> getRoleCodes() { return roleCodes; }
    public UserItemResponse setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; return this; }

    public String getCreatedAt() { return createdAt; }
    public UserItemResponse setCreatedAt(String createdAt) { this.createdAt = createdAt; return this; }

    public String getLastLoginAt() { return lastLoginAt; }
    public UserItemResponse setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }
}
