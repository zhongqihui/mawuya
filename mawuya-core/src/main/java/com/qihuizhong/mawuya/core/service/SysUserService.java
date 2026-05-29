/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.core.entity.SysRole;
import com.qihuizhong.mawuya.core.entity.SysUser;
import com.qihuizhong.mawuya.core.mapper.SysRoleMapper;
import com.qihuizhong.mawuya.core.mapper.SysUserMapper;
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
 * @author 钟启辉
 */
@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Autowired
    public SysUserService(SysUserMapper userMapper, SysRoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    /** 登录使用：根据用户名加载（含角色 codes） */
    public SysUser loadByUsername(String username) {
        if (StringUtils.isBlank(username)) return null;
        SysUser u = userMapper.selectByUsername(username.trim());
        if (u != null) {
            u.setRoleCodes(roleMapper.selectRoleCodesByUserSn(u.getSn()));
        }
        return u;
    }

    public SysUser loadById(Integer sn) {
        if (sn == null) return null;
        SysUser u = userMapper.selectById(sn);
        if (u != null) {
            u.setRoleCodes(roleMapper.selectRoleCodesByUserSn(u.getSn()));
        }
        return u;
    }

    public List<SysUser> page(String keyword, int page, int size) {
        int safePage = Math.max(1, page);
        int safeSize = (size < 1 || size > 200) ? 20 : size;
        int offset = (safePage - 1) * safeSize;
        String kw = blankToNull(keyword);
        List<SysUser> list = userMapper.selectPage(kw, offset, safeSize);
        if (list == null) return Collections.emptyList();
        // 批量补 roleCodes
        for (SysUser u : list) {
            u.setRoleCodes(roleMapper.selectRoleCodesByUserSn(u.getSn()));
            u.setPasswordHash(null); // 永远不向上层泄漏密码哈希
        }
        return list;
    }

    public int count(String keyword) {
        return userMapper.countAll(blankToNull(keyword));
    }

    /**
     * 创建用户 + 绑定角色（事务）。
     * @param user        含 username / passwordHash(BCrypt) / nickname / email / enabled
     * @param roleCodes   角色 code 列表（可空）
     * @return 新建用户 sn
     */
    @Transactional
    public int create(SysUser user, List<String> roleCodes) {
        if (user == null || StringUtils.isBlank(user.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (StringUtils.isBlank(user.getPasswordHash())) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (userMapper.selectByUsername(user.getUsername().trim()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        user.setUsername(user.getUsername().trim());
        if (user.getEnabled() == null) user.setEnabled(1);
        userMapper.insert(user);
        bindRoles(user.getSn(), roleCodes);
        return user.getSn();
    }

    /**
     * 更新用户 + 重新绑定角色（事务）；不修改密码。
     * @param roleCodes null 表示不修改角色绑定；空列表表示清空所有角色
     */
    @Transactional
    public void update(SysUser user, List<String> roleCodes) {
        if (user == null || user.getSn() == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        SysUser exist = userMapper.selectById(user.getSn());
        if (exist == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        userMapper.update(user);
        if (roleCodes != null) {
            bindRoles(user.getSn(), roleCodes);
        }
    }

    /** 修改密码（已加密的 hash） */
    public void resetPasswordHash(Integer sn, String newHash) {
        if (sn == null || StringUtils.isBlank(newHash)) {
            throw new IllegalArgumentException("参数错误");
        }
        userMapper.updatePasswordHash(sn, newHash);
    }

    /** 启用/禁用 */
    public void setEnabled(Integer sn, boolean enabled) {
        if (sn == null) throw new IllegalArgumentException("sn 不能为空");
        userMapper.updateEnabled(sn, enabled ? 1 : 0);
    }

    /** 删除用户（事务，连带删除关联） */
    @Transactional
    public void delete(Integer sn) {
        if (sn == null) throw new IllegalArgumentException("sn 不能为空");
        roleMapper.deleteUserRoles(sn);
        userMapper.deleteById(sn);
    }

    public void touchLastLogin(Integer sn) {
        if (sn != null) userMapper.updateLastLoginAt(sn);
    }

    public List<SysRole> listAllRoles() {
        return roleMapper.selectAll();
    }

    // ---------------- 私有 ----------------

    /** 重置某用户的角色绑定（先删后插） */
    private void bindRoles(Integer userSn, List<String> roleCodes) {
        roleMapper.deleteUserRoles(userSn);
        if (roleCodes == null || roleCodes.isEmpty()) return;
        // 去重 + 大写归一
        List<String> codes = roleCodes.stream()
                .filter(StringUtils::isNotBlank)
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) return;
        List<SysRole> roles = roleMapper.selectByCodes(codes);
        if (roles == null || roles.isEmpty()) return;
        List<Integer> roleSns = new ArrayList<>(roles.size());
        for (SysRole r : roles) roleSns.add(r.getSn());
        roleMapper.insertUserRoles(userSn, roleSns);
    }

    private static String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
