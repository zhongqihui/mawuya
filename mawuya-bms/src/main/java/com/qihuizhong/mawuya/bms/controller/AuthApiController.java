/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.bms.security.JwtUtil;
import com.qihuizhong.mawuya.core.entity.SysUser;
import com.qihuizhong.mawuya.core.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BMS REST API 鉴权入口（JSON）。
 *
 * <ul>
 *   <li>{@code POST /bms/api/auth/login} —— 用户名密码换 JWT；放行</li>
 *   <li>{@code GET  /bms/api/auth/me}    —— 当前登录信息（需要 token / session）</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/auth")
public class AuthApiController {

    @Autowired private SysUserService  sysUserService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil         jwtUtil;

    @PostMapping("login")
    public Map<String, Object> login(@RequestParam("username") String username,
                                     @RequestParam("password") String password) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            res.put("success", 0);
            res.put("message", "用户名或密码不能为空");
            return res;
        }
        SysUser u = sysUserService.loadByUsername(username.trim());
        if (u == null || u.getPasswordHash() == null
                || !passwordEncoder.matches(password, u.getPasswordHash())) {
            res.put("success", 0);
            res.put("message", "用户名或密码错误");
            return res;
        }
        if (u.getEnabled() == null || u.getEnabled() != 1) {
            res.put("success", 0);
            res.put("message", "账号已禁用");
            return res;
        }
        sysUserService.touchLastLogin(u.getSn());

        String token = jwtUtil.issue(u.getSn(), u.getUsername(),
                u.getRoleCodes() == null ? Collections.emptyList() : u.getRoleCodes());
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("sn",       u.getSn());
        userInfo.put("username", u.getUsername());
        userInfo.put("nickname", u.getNickname());
        userInfo.put("roles",    u.getRoleCodes());

        res.put("success", 1);
        res.put("token",   token);
        res.put("user",    userInfo);
        return res;
    }

    @GetMapping("me")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> me(Authentication auth) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (auth == null) {
            res.put("success", 0);
            res.put("message", "未登录");
            return res;
        }
        List<String> roles = auth.getAuthorities() == null ? Collections.emptyList()
                : auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                    .collect(Collectors.toList());
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("username", auth.getName());
        info.put("roles",    roles);
        res.put("success", 1);
        res.put("user",    info);
        return res;
    }
}
