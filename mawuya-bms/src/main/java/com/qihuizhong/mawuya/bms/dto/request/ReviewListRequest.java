/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 评论审批列表查询入参（{@code GET /bms/api/review/page}）。
 *
 * <p>当前实现是单页 200 条，未做真正分页（业务量足够），保留 status / page / size 三个字段。</p>
 *
 * @author 钟启辉
 */
public class ReviewListRequest {

    /** 0=PENDING 1=APPROVED 2=REJECTED；空字符串/null/"all"=全部 */
    private String status = "0";

    @Min(value = 1, message = "页码不能小于 1")
    private int page = 1;

    @Min(value = 1, message = "每页大小不能小于 1")
    @Max(value = 200, message = "每页大小不能超过 200")
    private int size = 200;

    public String getStatus() { return status; }
    public ReviewListRequest setStatus(String status) { this.status = status; return this; }

    public int getPage() { return page; }
    public ReviewListRequest setPage(Integer page) {
        this.page = (page == null || page < 1) ? 1 : page;
        return this;
    }

    public int getSize() { return size; }
    public ReviewListRequest setSize(Integer size) {
        this.size = (size == null || size < 1) ? 200 : Math.min(size, 200);
        return this;
    }

    /** 把字符串 status 解析为合法 Integer；非法或 "all" 返回 null（表示不过滤） */
    public Integer resolveStatusFilter() {
        if (status == null || "all".equalsIgnoreCase(status)) return null;
        try {
            int s = Integer.parseInt(status);
            return (s >= 0 && s <= 2) ? s : Integer.valueOf(0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
