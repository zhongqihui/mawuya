/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.mapper;

import com.qihuizhong.mawuya.core.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户 Mapper
 *
 * @author 钟启辉
 */
@Mapper
public interface SysUserMapper {

    /** 按用户名查（用于登录），返回的对象不带 roleCodes，需 service 层组装 */
    SysUser selectByUsername(@Param("username") String username);

    SysUser selectById(@Param("sn") Integer sn);

    /** 按关键词模糊匹配 username/nickname/email 的分页查询 */
    List<SysUser> selectPage(@Param("keyword") String keyword,
                             @Param("offset")  Integer offset,
                             @Param("limit")   Integer limit);

    int countAll(@Param("keyword") String keyword);

    int insert(SysUser user);

    int update(SysUser user);

    int updatePasswordHash(@Param("sn") Integer sn, @Param("hash") String hash);

    int updateEnabled(@Param("sn") Integer sn, @Param("enabled") Integer enabled);

    int updateLastLoginAt(@Param("sn") Integer sn);

    int deleteById(@Param("sn") Integer sn);
}
