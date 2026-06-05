/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.dataobject;

import java.io.Serializable;
import java.util.List;

/**
 * 系统用户数据对象（与数据库表 {@code sys_user} 对应）。
 *
 * <p>包含基础信息 + 启停状态 + 关联角色列表（非表字段，由 service 层组装）。
 * {@code toString()} 出于安全考虑不输出 passwordHash。</p>
 *
 * @author 钟启辉
 */
public class UserDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户主键 sn */
    private Integer sn;
    /** 登录用户名 */
    private String  username;
    /** BCrypt 哈希后的密码；查询时为安全起见，部分接口会 setPasswordHash(null) 后再返回 */
    private String  passwordHash;
    /** 昵称 */
    private String  nickname;
    /** 邮箱 */
    private String  email;
    /** 1=启用, 0=禁用 */
    private Integer enabled;
    /** 上次登录时间 */
    private String  lastLoginAt;
    /** 创建时间 */
    private String  createdAt;
    /** 更新时间 */
    private String  updatedAt;

    /** 关联角色 code 列表，例如 ["ADMIN", "EDITOR"]（非表字段，由 service 组装） */
    private List<String> roleCodes;

    public UserDO() {
    }

    public Integer getSn() {
        return sn;
    }

    public UserDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserDO setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserDO setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public UserDO setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserDO setEmail(String email) {
        this.email = email;
        return this;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public UserDO setEnabled(Integer enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getLastLoginAt() {
        return lastLoginAt;
    }

    public UserDO setLastLoginAt(String lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public UserDO setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public UserDO setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public UserDO setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
        return this;
    }

    @Override
    public String toString() {
        // 安全要求：不输出 passwordHash
        return "UserDO{" +
                "sn=" + sn +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", enabled=" + enabled +
                ", lastLoginAt='" + lastLoginAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", roleCodes=" + roleCodes +
                '}';
    }
}
