/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.dto.request;

import javax.validation.constraints.Min;

/**
 * 通用分页查询请求基类。
 *
 * <p>所有分页 API 的 Request DTO 都应继承此类（或包含同名字段），统一前端契约：</p>
 * <ul>
 *   <li>{@code page} 页码（默认 1）</li>
 *   <li>{@code size} 每页大小（默认 20，上限由调用方收敛）</li>
 * </ul>
 *
 * @author 钟启辉
 */
public class PageRequest {

    /** 页码，从 1 起。&lt; 1 视为 1 */
    @Min(value = 1, message = "页码不能小于 1")
    private int page = 1;

    /** 每页大小。建议 [1, 200]，调用方应在 service 层做最终收敛 */
    @Min(value = 1, message = "每页大小不能小于 1")
    private int size = 20;

    public int getPage() { return page; }
    public PageRequest setPage(Integer page) {
        this.page = (page == null || page < 1) ? 1 : page;
        return this;
    }

    public int getSize() { return size; }
    public PageRequest setSize(Integer size) {
        this.size = (size == null || size < 1) ? 20 : size;
        return this;
    }

    /** 计算 SQL OFFSET */
    public int offset() {
        return (page - 1) * size;
    }
}
