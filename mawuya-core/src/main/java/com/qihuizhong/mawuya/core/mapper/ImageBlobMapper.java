/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.entity.ImageBlob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 图片二进制存储 Mapper
 *
 * @author 钟启辉
 */
@Mapper
public interface ImageBlobMapper {

    /** 插入图片，返回主键写回 entity.sn */
    int insert(ImageBlob blob);

    /** 按 sn 查图片 metadata（不带二进制，便于列表展示） */
    ImageBlob selectMetaById(@Param("sn") Long sn);

    /** 按 sn 查完整图片（含二进制） */
    ImageBlob selectFullById(@Param("sn") Long sn);

    /** 按 sha256 去重查询 */
    ImageBlob selectBySha256(@Param("sha256") String sha256);

    /** 按 sourceUrl 查 sn（迁移时去重用） */
    Long selectSnBySourceUrl(@Param("sourceUrl") String sourceUrl);

    /** 列出所有 metadata（不含二进制） */
    List<ImageBlob> listMeta(@Param("limit") Integer limit, @Param("offset") Integer offset);

    int countAll();

    int deleteById(@Param("sn") Long sn);
}
