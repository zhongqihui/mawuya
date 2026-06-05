/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.dataobject;

import java.io.Serializable;

/**
 * 图片二进制数据对象（与数据库表 {@code image_blob} 对应）。
 *
 * <p>{@code toString()} 不输出 {@code data} 字节数组，避免日志爆炸。</p>
 *
 * @author 钟启辉
 */
public class ImageDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 图片主键 sn */
    private Long sn;
    /** 原始文件名 */
    private String fileName;
    /** Content-Type，例 image/png、image/jpeg */
    private String contentType;
    /** 字节数 */
    private Long byteSize;
    /** 二进制数据 */
    private byte[] data;
    /** SHA-256 摘要（用于秒传 + ETag） */
    private String sha256;
    /** 来源 URL（外链下载场景使用） */
    private String sourceUrl;
    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createdTime;

    public ImageDO() {
    }

    public Long getSn() {
        return sn;
    }

    public ImageDO setSn(Long sn) {
        this.sn = sn;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public ImageDO setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ImageDO setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Long getByteSize() {
        return byteSize;
    }

    public ImageDO setByteSize(Long byteSize) {
        this.byteSize = byteSize;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public ImageDO setData(byte[] data) {
        this.data = data;
        return this;
    }

    public String getSha256() {
        return sha256;
    }

    public ImageDO setSha256(String sha256) {
        this.sha256 = sha256;
        return this;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public ImageDO setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public ImageDO setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    @Override
    public String toString() {
        return "ImageDO{" +
                "sn=" + sn +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", byteSize=" + byteSize +
                ", sha256='" + sha256 + '\'' +
                ", createdTime='" + createdTime + '\'' +
                '}';
    }
}
