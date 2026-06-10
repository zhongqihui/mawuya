/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 文章「撤回」请求 DTO（{@code POST /bms/api/article/withdraw}）。
 *
 * @author 钟启辉
 */
public class ArticleWithdrawRequest {

    @NotNull(message = "sn 不能为空")
    private Integer sn;

    /** 撤回原因（可选，最长 200 字符，会落入状态流转日志的 remark 字段） */
    @Size(max = 200, message = "撤回原因最长 200 字符")
    private String remark;

    public Integer getSn() { return sn; }
    public ArticleWithdrawRequest setSn(Integer sn) { this.sn = sn; return this; }

    public String getRemark() { return remark; }
    public ArticleWithdrawRequest setRemark(String remark) { this.remark = remark; return this; }
}
