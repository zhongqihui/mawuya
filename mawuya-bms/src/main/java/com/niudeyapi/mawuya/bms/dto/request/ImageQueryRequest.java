/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.request;

import com.niudeyapi.mawuya.core.dto.request.PageRequest;

import javax.validation.constraints.Size;

/**
 * 图片库分页查询请求 DTO（{@code GET /bms/api/image/page}）。
 *
 * @author 钟启辉
 */
public class ImageQueryRequest extends PageRequest {

    @Size(max = 128, message = "关键字过长")
    private String keyword;

    public String getKeyword() { return keyword; }
    public ImageQueryRequest setKeyword(String keyword) { this.keyword = keyword; return this; }
}
