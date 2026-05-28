/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.entity;

/**
 * 图片二进制存储实体
 *
 * @author 钟启辉
 */
public class ImageBlob {

    private Long sn;
    private String fileName;
    private String contentType;
    private Long byteSize;
    private byte[] data;
    private String sha256;
    private String sourceUrl;
    private String createdTime;

    public Long getSn() { return sn; }
    public ImageBlob setSn(Long sn) { this.sn = sn; return this; }

    public String getFileName() { return fileName; }
    public ImageBlob setFileName(String fileName) { this.fileName = fileName; return this; }

    public String getContentType() { return contentType; }
    public ImageBlob setContentType(String contentType) { this.contentType = contentType; return this; }

    public Long getByteSize() { return byteSize; }
    public ImageBlob setByteSize(Long byteSize) { this.byteSize = byteSize; return this; }

    public byte[] getData() { return data; }
    public ImageBlob setData(byte[] data) { this.data = data; return this; }

    public String getSha256() { return sha256; }
    public ImageBlob setSha256(String sha256) { this.sha256 = sha256; return this; }

    public String getSourceUrl() { return sourceUrl; }
    public ImageBlob setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; return this; }

    public String getCreatedTime() { return createdTime; }
    public ImageBlob setCreatedTime(String createdTime) { this.createdTime = createdTime; return this; }
}
