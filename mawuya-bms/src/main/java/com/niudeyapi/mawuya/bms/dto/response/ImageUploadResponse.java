/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 图片上传成功响应 DTO（仅站内 /bms/api/image/upload 使用，
 * editor.md 上传接口因第三方契约保留旧返回格式，不使用此 DTO）。
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageUploadResponse {

    private Long sn;
    private String url;
    private String name;
    private Long size;

    public ImageUploadResponse() {}

    public ImageUploadResponse(Long sn, String url, String name, Long size) {
        this.sn = sn;
        this.url = url;
        this.name = name;
        this.size = size;
    }

    public Long getSn() { return sn; }
    public ImageUploadResponse setSn(Long sn) { this.sn = sn; return this; }

    public String getUrl() { return url; }
    public ImageUploadResponse setUrl(String url) { this.url = url; return this; }

    public String getName() { return name; }
    public ImageUploadResponse setName(String name) { this.name = name; return this; }

    public Long getSize() { return size; }
    public ImageUploadResponse setSize(Long size) { this.size = size; return this; }
}
