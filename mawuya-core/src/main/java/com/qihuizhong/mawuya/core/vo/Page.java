/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页 VO
 *
 * @author zqh
 */
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总页数 */
    private int pageSize;
    /** 当前页 */
    private int curr;
    /** 给前台展示的页码轴 */
    private List<String> pageLine;
    /** 每页展示条数 */
    private int size;
    /** 响应路径，供前台分页 */
    private String url;
    /** 当前页记录 */
    private List<T> lists;

    public Page() {
    }

    public int getPageSize() {
        return pageSize;
    }

    public Page<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getCurr() {
        return curr;
    }

    public Page<T> setCurr(int curr) {
        this.curr = curr;
        return this;
    }

    public List<String> getPageLine() {
        return pageLine;
    }

    public Page<T> setPageLine(List<String> pageLine) {
        this.pageLine = pageLine;
        return this;
    }

    public int getSize() {
        return size;
    }

    public Page<T> setSize(int size) {
        this.size = size;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Page<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    public List<T> getLists() {
        return lists;
    }

    public Page<T> setLists(List<T> lists) {
        this.lists = lists;
        return this;
    }
}
