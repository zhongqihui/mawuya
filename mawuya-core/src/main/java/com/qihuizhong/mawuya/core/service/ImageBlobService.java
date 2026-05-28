/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.ImageBlob;
import com.qihuizhong.mawuya.core.mapper.ImageBlobMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.List;

/**
 * 图片二进制服务：负责将上传字节落库 + 读取，并按 sha256 去重。
 *
 * @author 钟启辉
 */
@Service
public class ImageBlobService {

    @Autowired
    private ImageBlobMapper imageBlobMapper;

    /**
     * 保存图片到 DB；如已存在相同 sha256 则复用其 sn（去重）
     *
     * @return 落库后的主键 sn
     */
    public long save(byte[] data, String fileName, String contentType, String sourceUrl) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("empty image data");
        }
        String sha = sha256Hex(data);
        ImageBlob exist = imageBlobMapper.selectBySha256(sha);
        if (exist != null) {
            return exist.getSn();
        }
        ImageBlob entity = new ImageBlob()
                .setFileName(fileName == null ? "unknown" : truncate(fileName, 160))
                .setContentType(contentType == null ? "application/octet-stream" : truncate(contentType, 60))
                .setByteSize((long) data.length)
                .setData(data)
                .setSha256(sha)
                .setSourceUrl(sourceUrl == null ? null : truncate(sourceUrl, 500));
        imageBlobMapper.insert(entity);
        return entity.getSn();
    }

    /** 已存在的 sourceUrl，直接复用 sn（迁移用，避免重复下载/入库） */
    public Long findSnBySourceUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isEmpty()) return null;
        return imageBlobMapper.selectSnBySourceUrl(sourceUrl);
    }

    public ImageBlob loadFull(Long sn) {
        return imageBlobMapper.selectFullById(sn);
    }

    public ImageBlob loadMeta(Long sn) {
        return imageBlobMapper.selectMetaById(sn);
    }

    public List<ImageBlob> listMeta(int limit, int offset) {
        return imageBlobMapper.listMeta(limit, offset);
    }

    public int countAll() {
        return imageBlobMapper.countAll();
    }

    /**
     * 按关键词分页查询图片 metadata。
     * keyword 为 null/空白等价于 listMeta；offset/limit 由调用方做合法性收敛。
     */
    public List<ImageBlob> listMetaByKeyword(String keyword, int limit, int offset) {
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return imageBlobMapper.listMetaByKeyword(kw, limit, offset);
    }

    /** 与 listMetaByKeyword 配套的总数查询 */
    public int countByKeyword(String keyword) {
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return imageBlobMapper.countByKeyword(kw);
    }

    public boolean deleteById(Long sn) {
        return imageBlobMapper.deleteById(sn) > 0;
    }

    // -------------------- helpers --------------------
    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("sha256 failed", e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
