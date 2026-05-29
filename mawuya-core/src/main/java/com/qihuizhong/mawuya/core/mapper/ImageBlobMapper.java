/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.dataobject.ImageDO;
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
    int insert(ImageDO blob);

    /** 按 sn 查图片 metadata（不带二进制，便于列表展示） */
    ImageDO selectMetaById(@Param("sn") Long sn);

    /** 按 sn 查完整图片（含二进制） */
    ImageDO selectFullById(@Param("sn") Long sn);

    /** 按 sha256 去重查询 */
    ImageDO selectBySha256(@Param("sha256") String sha256);

    /** 按 sourceUrl 查 sn（迁移时去重用） */
    Long selectSnBySourceUrl(@Param("sourceUrl") String sourceUrl);

    /** 列出所有 metadata（不含二进制） */
    List<ImageDO> listMeta(@Param("limit") Integer limit, @Param("offset") Integer offset);

    int countAll();

    /**
     * 按关键词模糊匹配 file_name 列出 metadata（不含二进制）。
     * keyword 为 null/空则等价于 listMeta，按 sn DESC 排序。
     * <p>注意：keyword 由 mapper 中通过 #{keyword} 参数化绑定，xml 内部仅做 CONCAT('%', #{keyword}, '%')，
     * 不存在 SQL 字符串拼接，已防注入。</p>
     */
    List<ImageDO> listMetaByKeyword(@Param("keyword") String keyword,
                                      @Param("limit") Integer limit,
                                      @Param("offset") Integer offset);

    /** 与 listMetaByKeyword 配套，返回匹配总数。keyword 为 null/空则等价于 countAll。 */
    int countByKeyword(@Param("keyword") String keyword);

    int deleteById(@Param("sn") Long sn);
}
