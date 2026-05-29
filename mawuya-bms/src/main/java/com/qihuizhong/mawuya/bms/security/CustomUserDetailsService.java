/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.security;

import com.qihuizhong.mawuya.core.entity.SysUser;
import com.qihuizhong.mawuya.core.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 桥接 Spring Security 与 sys_user 表。
 *
 * <p>登录认证时由 DaoAuthenticationProvider 调用 {@link #loadUserByUsername}：</p>
 * <ul>
 *   <li>用户不存在 → 抛 UsernameNotFoundException</li>
 *   <li>已禁用 → 返回 enabled=false 的 User，Spring Security 会抛 DisabledException</li>
 *   <li>角色 code 自动加 {@code ROLE_} 前缀，符合 Spring Security 默认约定</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser u = sysUserService.loadByUsername(username);
        if (u == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        List<String> codes = u.getRoleCodes() == null ? Collections.emptyList() : u.getRoleCodes();
        for (String c : codes) {
            if (c == null || c.isEmpty()) continue;
            authorities.add(new SimpleGrantedAuthority("ROLE_" + c.toUpperCase()));
        }
        boolean enabled = u.getEnabled() != null && u.getEnabled() == 1;
        return User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .authorities(authorities)
                .disabled(!enabled)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
