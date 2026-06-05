/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页响应 DTO。
 *
 * <p>所有列表分页接口的返回 data 统一为此结构，前端按字段名读取，
 * 不再每个接口手写 {@code list/total/page/size/pages} 这种 Map。</p>
 *
 * <pre>
 *   BaseResponse&lt;PageResponse&lt;UserDto&gt;&gt;
 *     - code: "000000"
 *     - message: "成功"
 *     - data:
 *         - list:  [...]
 *         - total: 132
 *         - page:  1
 *         - size:  20
 *         - pages: 7
 * </pre>
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> list;
    /** 总记录数 */
    private long total;
    /** 当前页码（从 1 起） */
    private int page;
    /** 每页大小 */
    private int size;
    /** 总页数 */
    private int pages;

    public PageResponse() {
    }

    public PageResponse(List<T> list, long total, int page, int size) {
        this.list = list == null ? Collections.emptyList() : list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size <= 0 ? 0 : (int) ((total + size - 1) / size);
    }

    public static <T> PageResponse<T> of(List<T> list, long total, int page, int size) {
        return new PageResponse<>(list, total, page, size);
    }

    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(Collections.emptyList(), 0L, page, size);
    }

    public List<T> getList() { return list; }
    public PageResponse<T> setList(List<T> list) { this.list = list; return this; }

    public long getTotal() { return total; }
    public PageResponse<T> setTotal(long total) { this.total = total; return this; }

    public int getPage() { return page; }
    public PageResponse<T> setPage(int page) { this.page = page; return this; }

    public int getSize() { return size; }
    public PageResponse<T> setSize(int size) { this.size = size; return this; }

    public int getPages() { return pages; }
    public PageResponse<T> setPages(int pages) { this.pages = pages; return this; }
}
