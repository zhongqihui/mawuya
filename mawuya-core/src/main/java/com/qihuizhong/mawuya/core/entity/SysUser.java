/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 系统用户（与 sys_user 表对应）
 *
 * <p>包含基础信息 + 启停状态 + 关联角色列表（非表字段，由 service 层组装）。</p>
 *
 * @author 钟启辉
 */
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sn;
    private String  username;
    /** BCrypt 哈希后的密码；查询时为安全起见，部分接口会 setPasswordHash(null) 后再返回 */
    private String  passwordHash;
    private String  nickname;
    private String  email;
    /** 1=启用, 0=禁用 */
    private Integer enabled;
    private String  lastLoginAt;
    private String  createdAt;
    private String  updatedAt;

    /** 关联角色 code 列表，例如 ["ADMIN", "EDITOR"]（非表字段，由 service 组装） */
    private List<String> roleCodes;

    public Integer getSn() { return sn; }
    public SysUser setSn(Integer sn) { this.sn = sn; return this; }

    public String getUsername() { return username; }
    public SysUser setUsername(String username) { this.username = username; return this; }

    public String getPasswordHash() { return passwordHash; }
    public SysUser setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }

    public String getNickname() { return nickname; }
    public SysUser setNickname(String nickname) { this.nickname = nickname; return this; }

    public String getEmail() { return email; }
    public SysUser setEmail(String email) { this.email = email; return this; }

    public Integer getEnabled() { return enabled; }
    public SysUser setEnabled(Integer enabled) { this.enabled = enabled; return this; }

    public String getLastLoginAt() { return lastLoginAt; }
    public SysUser setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }

    public String getCreatedAt() { return createdAt; }
    public SysUser setCreatedAt(String createdAt) { this.createdAt = createdAt; return this; }

    public String getUpdatedAt() { return updatedAt; }
    public SysUser setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; return this; }

    public List<String> getRoleCodes() { return roleCodes; }
    public SysUser setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; return this; }
}
