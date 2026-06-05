/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.core.service;

import com.mawuya.core.dataobject.RoleDO;
import com.mawuya.core.dataobject.UserDO;
import com.mawuya.core.enums.ResultCodeEnum;
import com.mawuya.core.exception.BusinessException;
import com.mawuya.core.mapper.SysRoleMapper;
import com.mawuya.core.mapper.SysUserMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户业务层。
 *
 * <p>纯字段处理：不强依赖 spring-security。密码加密 / 校验由 BMS 调用方使用 BCrypt 完成，
 * 本服务接收已加密的 passwordHash 直接落库，避免 core 引入 security 依赖污染 AMS。</p>
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code get/list/count/save/update/remove}；
 * 参数校验失败抛 {@link BusinessException}（替代 {@code IllegalArgumentException}），
 * 由全局异常处理器自动转换为 BaseResponse(A0400)。</p>
 *
 * @author 钟启辉
 */
@Service
public class SysUserService {

    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 20;
    /** 单页最大条数 */
    private static final int MAX_PAGE_SIZE = 200;
    /** UserDO.enabled 启用值 */
    private static final int ENABLED_VALUE  = 1;
    /** UserDO.enabled 禁用值 */
    private static final int DISABLED_VALUE = 0;

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Autowired
    public SysUserService(SysUserMapper userMapper, SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    /** 登录使用：根据用户名加载（含角色 codes）。 */
    public UserDO getByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        UserDO u = userMapper.selectByUsername(username.trim());
        if (u != null) {
            u.setRoleCodes(roleMapper.selectRoleCodesByUserSn(u.getSn()));
        }
        return u;
    }

    /** 按主键加载（含角色 codes）。 */
    public UserDO getById(Integer sn) {
        if (sn == null) {
            return null;
        }
        UserDO u = userMapper.selectById(sn);
        if (u != null) {
            u.setRoleCodes(roleMapper.selectRoleCodesByUserSn(u.getSn()));
        }
        return u;
    }

    /** 关键字 + 分页查询（passwordHash 不向上层暴露）。 */
    public List<UserDO> listByPage(String keyword, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = (size < 1 || size > MAX_PAGE_SIZE) ? DEFAULT_PAGE_SIZE : size;
        int offset = (safePage - 1) * safeSize;
        String kw = blankToNull(keyword);
        List<UserDO> list = userMapper.selectPage(kw, offset, safeSize);
        if (list == null) {
            return Collections.emptyList();
        }
        // 批量补 roleCodes
        for (UserDO u : list) {
            u.setRoleCodes(roleMapper.selectRoleCodesByUserSn(u.getSn()));
            // 永远不向上层泄漏密码哈希
            u.setPasswordHash(null);
        }
        return list;
    }

    /** 关键字计数。 */
    public int count(String keyword) {
        return userMapper.countAll(blankToNull(keyword));
    }

    /**
     * 创建用户 + 绑定角色（事务）。
     *
     * @param user      含 username / passwordHash(BCrypt) / nickname / email / enabled
     * @param roleCodes 角色 code 列表（可空）
     * @return 新建用户 sn
     */
    @Transactional
    public int save(UserDO user, List<String> roleCodes) {
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "用户名不能为空");
        }
        if (StringUtils.isBlank(user.getPasswordHash())) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "密码不能为空");
        }
        if (userMapper.selectByUsername(user.getUsername().trim()) != null) {
            throw new BusinessException(ResultCodeEnum.CONFLICT, "用户名已存在");
        }
        user.setUsername(user.getUsername().trim());
        if (user.getEnabled() == null) {
            user.setEnabled(ENABLED_VALUE);
        }
        userMapper.insert(user);
        bindRoles(user.getSn(), roleCodes);
        return user.getSn();
    }

    /**
     * 更新用户 + 重新绑定角色（事务）；不修改密码。
     *
     * @param roleCodes null 表示不修改角色绑定；空列表表示清空所有角色
     */
    @Transactional
    public void updateById(UserDO user, List<String> roleCodes) {
        if (user == null || user.getSn() == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "用户不存在");
        }
        UserDO exist = userMapper.selectById(user.getSn());
        if (exist == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND, "用户不存在");
        }
        userMapper.update(user);
        if (roleCodes != null) {
            bindRoles(user.getSn(), roleCodes);
        }
    }

    /** 重置密码（已加密的 hash）。 */
    public void updatePasswordHashById(Integer sn, String newHash) {
        if (sn == null || StringUtils.isBlank(newHash)) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "参数错误");
        }
        userMapper.updatePasswordHash(sn, newHash);
    }

    /** 启用/禁用。 */
    public void updateEnabledById(Integer sn, boolean enabled) {
        if (sn == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "sn 不能为空");
        }
        userMapper.updateEnabled(sn, enabled ? ENABLED_VALUE : DISABLED_VALUE);
    }

    /** 删除用户（事务，连带解绑角色）。 */
    @Transactional
    public void removeById(Integer sn) {
        if (sn == null) {
            throw new BusinessException(ResultCodeEnum.PARAM_INVALID, "sn 不能为空");
        }
        roleMapper.deleteUserRoles(sn);
        userMapper.deleteById(sn);
    }

    /** 刷新最近登录时间。 */
    public void updateLastLoginAt(Integer sn) {
        if (sn != null) {
            userMapper.updateLastLoginAt(sn);
        }
    }

    /** 列出所有角色。 */
    public List<RoleDO> listAllRoles() {
        return roleMapper.selectAll();
    }

    // ====================================================================
    // 私有
    // ====================================================================

    /** 重置某用户的角色绑定（先删后插）。 */
    private void bindRoles(Integer userSn, List<String> roleCodes) {
        roleMapper.deleteUserRoles(userSn);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        // 去重 + 大写归一
        List<String> codes = roleCodes.stream()
                .filter(StringUtils::isNotBlank)
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) {
            return;
        }
        List<RoleDO> roles = roleMapper.selectByCodes(codes);
        if (roles == null || roles.isEmpty()) {
            return;
        }
        List<Integer> roleSns = new ArrayList<>(roles.size());
        for (RoleDO r : roles) {
            roleSns.add(r.getSn());
        }
        roleMapper.insertUserRoles(userSn, roleSns);
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
