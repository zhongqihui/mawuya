/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.controller;

import com.mawuya.bms.dto.request.LoginRequest;
import com.mawuya.bms.dto.response.LoginResponse;
import com.mawuya.bms.security.JwtUtil;
import com.mawuya.core.common.BaseResponse;
import com.mawuya.core.dataobject.UserDO;
import com.mawuya.core.enums.ResultCodeEnum;
import com.mawuya.core.exception.BusinessException;
import com.mawuya.core.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BMS 鉴权 JSON API。
 *
 * <h3>API 列表</h3>
 * <ul>
 *   <li>{@code POST /bms/api/auth/login}  用户名密码换 JWT（放行）</li>
 *   <li>{@code GET  /bms/api/auth/me}     当前登录信息（需要 token / session）</li>
 * </ul>
 *
 * <p>所有响应均为 {@link BaseResponse} 标准结构。失败抛 {@link BusinessException}
 * 由 {@code GlobalExceptionHandler} 统一兜底。</p>
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
    public BaseResponse<LoginResponse> login(@Valid LoginRequest req) {
        UserDO u = sysUserService.getByUsername(req.getUsername().trim());
        if (u == null || u.getPasswordHash() == null
                || !passwordEncoder.matches(req.getPassword(), u.getPasswordHash())) {
            throw new BusinessException(ResultCodeEnum.LOGIN_FAILED);
        }
        if (u.getEnabled() == null || u.getEnabled() != 1) {
            throw new BusinessException(ResultCodeEnum.ACCOUNT_DISABLED);
        }
        sysUserService.updateLastLoginAt(u.getSn());

        String token = jwtUtil.issue(u.getSn(), u.getUsername(),
                u.getRoleCodes() == null ? Collections.emptyList() : u.getRoleCodes());

        LoginResponse.UserBrief brief = new LoginResponse.UserBrief(
                u.getSn(), u.getUsername(), u.getNickname(), u.getRoleCodes());
        return BaseResponse.success(new LoginResponse(token, brief));
    }

    @GetMapping("me")
    @PreAuthorize("isAuthenticated()")
    public BaseResponse<LoginResponse.UserBrief> me(Authentication auth) {
        if (auth == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }
        List<String> roles = auth.getAuthorities() == null ? Collections.emptyList()
                : auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(s -> s.startsWith("ROLE_") ? s.substring(5) : s)
                    .collect(Collectors.toList());
        LoginResponse.UserBrief brief = new LoginResponse.UserBrief(
                null, auth.getName(), null, roles);
        return BaseResponse.success(brief);
    }
}
