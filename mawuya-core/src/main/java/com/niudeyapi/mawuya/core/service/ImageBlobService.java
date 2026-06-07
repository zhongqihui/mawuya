/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.service;

import com.niudeyapi.mawuya.core.dataobject.ImageDO;
import com.niudeyapi.mawuya.core.enums.ResultCodeEnum;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.mapper.ImageBlobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.List;

/**
 * 图片二进制业务层：负责将上传字节落库 + 读取，并按 sha256 去重。
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code get/list/count/save/remove}。</p>
 *
 * @author 钟启辉
 */
@Service
public class ImageBlobService {

    private static final Logger log = LoggerFactory.getLogger(ImageBlobService.class);

    /** SHA-256 算法名 */
    private static final String SHA_256 = "SHA-256";
    /** 默认未知文件名 */
    private static final String UNKNOWN_FILE_NAME = "unknown";
    /** 默认 Content-Type */
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    /** 文件名最大长度 */
    private static final int MAX_FILE_NAME = 160;
    /** Content-Type 最大长度 */
    private static final int MAX_CONTENT_TYPE = 60;
    /** sourceUrl 最大长度 */
    private static final int MAX_SOURCE_URL = 500;
    /** 16 进制低 4 位掩码 */
    private static final int LOW_4_BIT_MASK = 0xF;
    /** 16 进制 base */
    private static final int HEX_BASE = 16;

    @Autowired
    private ImageBlobMapper imageBlobMapper;

    /**
     * 保存图片到 DB；如已存在相同 sha256 则复用其 sn（去重）。
     *
     * @return 落库后的主键 sn
     */
    public long save(byte[] data, String fileName, String contentType, String sourceUrl) {
        if (data == null || data.length == 0) {
            throw new BusinessException(ResultCodeEnum.UPLOAD_INVALID, "empty image data");
        }
        String sha = sha256Hex(data);
        ImageDO exist = imageBlobMapper.selectBySha256(sha);
        if (exist != null) {
            return exist.getSn();
        }
        ImageDO entity = new ImageDO()
                .setFileName(fileName == null ? UNKNOWN_FILE_NAME : truncate(fileName, MAX_FILE_NAME))
                .setContentType(contentType == null ? DEFAULT_CONTENT_TYPE : truncate(contentType, MAX_CONTENT_TYPE))
                .setByteSize((long) data.length)
                .setData(data)
                .setSha256(sha)
                .setSourceUrl(sourceUrl == null ? null : truncate(sourceUrl, MAX_SOURCE_URL));
        try {
            imageBlobMapper.insert(entity);
        } catch (TransientDataAccessResourceException e) {
            // 典型场景：单条 INSERT 包大小 > MySQL 服务端 max_allowed_packet（默认仅 4MB）
            // 翻译成业务级 4xx，避免直接给前端 500，并在日志里给出排查指引
            if (isPacketTooBig(e)) {
                log.warn("[image-blob] insert failed: PacketTooBig, byteSize={}B (~{}KB). " +
                        "Server max_allowed_packet 太小，请在 MySQL 配置 my.cnf 设置 " +
                        "[mysqld] max_allowed_packet=64M 后重启容器；或临时执行 " +
                        "SET GLOBAL max_allowed_packet=67108864（仅作用于新连接）。",
                        data.length, data.length / 1024);
                throw new BusinessException(ResultCodeEnum.UPLOAD_TOO_LARGE,
                        "图片过大（" + (data.length / 1024 / 1024) + "MB），数据库 max_allowed_packet 限制不足，" +
                        "请联系管理员调大该参数或压缩图片后重试");
            }
            throw e;
        }
        return entity.getSn();
    }

    /** 递归判断异常链里是否含 PacketTooBigException（不直接 import 以避免 core 模块强依赖 mysql 驱动类）。 */
    private static boolean isPacketTooBig(Throwable t) {
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {
            String name = cur.getClass().getName();
            if (name.endsWith("PacketTooBigException")) {
                return true;
            }
            String msg = cur.getMessage();
            if (msg != null && msg.contains("max_allowed_packet")) {
                return true;
            }
        }
        return false;
    }

    /** 已存在的 sourceUrl 直接复用 sn（迁移用，避免重复下载/入库）。 */
    public Long getSnBySourceUrl(String sourceUrl) {
        if (sourceUrl == null || sourceUrl.isEmpty()) {
            return null;
        }
        return imageBlobMapper.selectSnBySourceUrl(sourceUrl);
    }

    /** 取完整图片（含字节数据）。 */
    public ImageDO getFullById(Long sn) {
        return imageBlobMapper.selectFullById(sn);
    }

    /** 取图片元数据（不含字节数据）。 */
    public ImageDO getMetaById(Long sn) {
        return imageBlobMapper.selectMetaById(sn);
    }

    /** 列出图片元数据。 */
    public List<ImageDO> listMeta(int limit, int offset) {
        return imageBlobMapper.listMeta(limit, offset);
    }

    /** 总数。 */
    public int count() {
        return imageBlobMapper.countAll();
    }

    /**
     * 按关键词分页查询图片 metadata。
     *
     * <p>keyword 为 null / 空白时等价于 listMeta；offset/limit 由调用方做合法性收敛。</p>
     */
    public List<ImageDO> listMetaByKeyword(String keyword, int limit, int offset) {
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return imageBlobMapper.listMetaByKeyword(kw, limit, offset);
    }

    /** 与 listMetaByKeyword 配套的总数查询。 */
    public int countByKeyword(String keyword) {
        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        return imageBlobMapper.countByKeyword(kw);
    }

    /** 按主键删除。 */
    public boolean removeById(Long sn) {
        return imageBlobMapper.deleteById(sn) > 0;
    }

    // ====================================================================
    // 内部工具方法
    // ====================================================================

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & LOW_4_BIT_MASK, HEX_BASE));
                sb.append(Character.forDigit(b & LOW_4_BIT_MASK, HEX_BASE));
            }
            return sb.toString();
        } catch (Exception e) {
            // 阿里规约：禁止裸抛 RuntimeException；改抛 BusinessException 携带错误码 B0001
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "sha256 计算失败：" + e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
