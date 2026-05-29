/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.request;

import com.qihuizhong.mawuya.core.dto.request.PageRequest;

import javax.validation.constraints.Size;

/**
 * 用户分页查询请求 DTO（{@code GET /bms/api/user/page}）。
 *
 * @author 钟启辉
 */
public class UserQueryRequest extends PageRequest {

    @Size(max = 64, message = "关键字过长")
    private String keyword;

    public String getKeyword() { return keyword; }
    public UserQueryRequest setKeyword(String keyword) { this.keyword = keyword; return this; }
}
