/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 系统角色数据对象（与数据库表 {@code sys_role} 对应）。
 *
 * @author 钟启辉
 */
public class RoleDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 角色主键 sn */
    private Integer sn;
    /** 角色 code，唯一 */
    private String  code;
    /** 角色显示名 */
    private String  name;
    /** 角色描述 */
    private String  description;
    /** 创建时间 */
    private String  createdAt;

    public RoleDO() {
    }

    public Integer getSn() {
        return sn;
    }

    public RoleDO setSn(Integer sn) {
        this.sn = sn;
        return this;
    }

    public String getCode() {
        return code;
    }

    public RoleDO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public RoleDO setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RoleDO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public RoleDO setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return "RoleDO{" +
                "sn=" + sn +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
