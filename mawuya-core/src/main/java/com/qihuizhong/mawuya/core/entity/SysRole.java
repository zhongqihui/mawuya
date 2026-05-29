/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

import java.io.Serializable;

/**
 * 系统角色（与 sys_role 表对应）
 *
 * @author 钟启辉
 */
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer sn;
    private String  code;
    private String  name;
    private String  description;
    private String  createdAt;

    public Integer getSn() { return sn; }
    public SysRole setSn(Integer sn) { this.sn = sn; return this; }

    public String getCode() { return code; }
    public SysRole setCode(String code) { this.code = code; return this; }

    public String getName() { return name; }
    public SysRole setName(String name) { this.name = name; return this; }

    public String getDescription() { return description; }
    public SysRole setDescription(String description) { this.description = description; return this; }

    public String getCreatedAt() { return createdAt; }
    public SysRole setCreatedAt(String createdAt) { this.createdAt = createdAt; return this; }
}
