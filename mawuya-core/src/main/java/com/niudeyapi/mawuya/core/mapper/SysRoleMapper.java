/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.mapper;

import com.niudeyapi.mawuya.core.dataobject.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色 Mapper
 *
 * @author 钟启辉
 */
@Mapper
public interface SysRoleMapper {

    List<RoleDO> selectAll();

    /** 列出某用户的所有角色 code（小写不敏感，但表里都按大写存） */
    List<String> selectRoleCodesByUserSn(@Param("userSn") Integer userSn);

    /** 列出某用户的角色明细对象 */
    List<RoleDO> selectRolesByUserSn(@Param("userSn") Integer userSn);

    /** 按 code 列表批量查 sn（用于绑定） */
    List<RoleDO> selectByCodes(@Param("codes") List<String> codes);

    /** 全量重置某用户的角色绑定：先 delete 再 insert，事务由 service 层控制 */
    int deleteUserRoles(@Param("userSn") Integer userSn);

    int insertUserRoles(@Param("userSn") Integer userSn,
                        @Param("roleSns") List<Integer> roleSns);
}
