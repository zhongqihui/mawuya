/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.dto.request;

import javax.validation.constraints.NotNull;

/**
 * BMS 通用 sn 请求 DTO：评论 approve/reject、文章/分类/图片删除等只用 sn 的接口共用。
 *
 * @author 钟启辉
 */
public class SnRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    public SnRequest() {}

    public SnRequest(Integer sn) {
        this.sn = sn;
    }

    public Integer getSn() { return sn; }
    public SnRequest setSn(Integer sn) { this.sn = sn; return this; }
}
