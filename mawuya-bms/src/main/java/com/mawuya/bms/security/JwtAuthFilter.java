/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 鉴权过滤器：
 * <ul>
 *   <li>仅作用于 {@code /bms/api/**} 路径（页面端走 form-login + session）</li>
 *   <li>从 {@code Authorization: Bearer xxx} 头读取 token</li>
 *   <li>校验通过 → 注入 SecurityContext 的 Authentication（含 ROLE_xxx 权限）</li>
 *   <li>校验失败/缺失 → 不抛异常，交给后续 filter，由权限规则决定是否 401</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** 仅介入此前缀的请求 */
    private static final String API_URI_PREFIX = "/bms/api/";
    /** Spring Security 默认角色前缀 */
    private static final String ROLE_PREFIX = "ROLE_";
    /** 匿名占位 principal */
    private static final String ANONYMOUS_USER = "anonymousUser";
    /** Authorization Header */
    private static final String AUTH_HEADER = "Authorization";

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        // 仅对 /bms/api/** 介入；页面路径完全交给 form-login + session
        String uri = req.getRequestURI();
        if (uri == null || !uri.startsWith(API_URI_PREFIX)) {
            chain.doFilter(req, resp);
            return;
        }
        // 已登录（form-login 的 session）则不要二次覆盖
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !ANONYMOUS_USER.equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            chain.doFilter(req, resp);
            return;
        }
        String header = req.getHeader(AUTH_HEADER);
        if (header == null || header.isEmpty()) {
            chain.doFilter(req, resp);
            return;
        }
        JwtUtil.JwtPayload payload = jwtUtil.parse(header);
        if (payload == null || payload.username == null) {
            chain.doFilter(req, resp);
            return;
        }
        // 转换 roles → ROLE_xxx GrantedAuthority
        List<String> roles = payload.roles;
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(roles == null ? 0 : roles.size());
        if (roles != null) {
            for (String r : roles) {
                if (r == null || r.isEmpty()) {
                    continue;
                }
                String upper = r.startsWith(ROLE_PREFIX) ? r : (ROLE_PREFIX + r.toUpperCase());
                authorities.add(new SimpleGrantedAuthority(upper));
            }
        }
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(payload.username, null, authorities);
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(req, resp);
    }
}
