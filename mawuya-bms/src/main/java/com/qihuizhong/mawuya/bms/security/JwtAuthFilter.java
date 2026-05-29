/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.security;

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

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        // 仅对 /bms/api/** 介入；页面路径完全交给 form-login + session
        String uri = req.getRequestURI();
        if (uri == null || !uri.startsWith("/bms/api/")) {
            chain.doFilter(req, resp);
            return;
        }
        // 已登录（form-login 的 session）则不要二次覆盖
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            chain.doFilter(req, resp);
            return;
        }
        String header = req.getHeader("Authorization");
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
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (payload.roles != null) {
            for (String r : payload.roles) {
                if (r == null || r.isEmpty()) continue;
                String upper = r.startsWith("ROLE_") ? r : ("ROLE_" + r.toUpperCase());
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
