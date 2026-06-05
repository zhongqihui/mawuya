/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 图片元数据 DTO（图片库列表 / 选择器返回 data.list 元素）。
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageMetaResponse {

    private Long sn;
    private String name;
    /** 访问 URL（恒为 /image/db/{sn}） */
    private String url;
    /** 字节大小 */
    private Long size;
    /** 落库时间 */
    private String mtime;

    public ImageMetaResponse() {}

    public ImageMetaResponse(Long sn, String name, String url, Long size, String mtime) {
        this.sn = sn;
        this.name = name;
        this.url = url;
        this.size = size;
        this.mtime = mtime;
    }

    public Long getSn() { return sn; }
    public ImageMetaResponse setSn(Long sn) { this.sn = sn; return this; }

    public String getName() { return name; }
    public ImageMetaResponse setName(String name) { this.name = name; return this; }

    public String getUrl() { return url; }
    public ImageMetaResponse setUrl(String url) { this.url = url; return this; }

    public Long getSize() { return size; }
    public ImageMetaResponse setSize(Long size) { this.size = size; return this; }

    public String getMtime() { return mtime; }
    public ImageMetaResponse setMtime(String mtime) { this.mtime = mtime; return this; }
}
