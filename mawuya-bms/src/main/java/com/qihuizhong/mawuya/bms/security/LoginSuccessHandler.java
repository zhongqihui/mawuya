/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.security;

import com.qihuizhong.mawuya.core.dataobject.UserDO;
import com.qihuizhong.mawuya.core.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 表单登录成功 handler：
 * 1) 写入 last_login_at
 * 2) 跳转到 BMS 主页 / 或登录前请求路径（Spring Security 自带 SavedRequest 机制）
 *
 * @author 钟启辉
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private SysUserService sysUserService;

    public LoginSuccessHandler() {
        super("/bms/index"); // 默认跳转
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse resp, Authentication auth)
            throws IOException, ServletException {
        try {
            if (auth != null && auth.getName() != null) {
                UserDO u = sysUserService.getByUsername(auth.getName());
                if (u != null) {
                    sysUserService.updateLastLoginAt(u.getSn());
                }
            }
        } catch (Exception ignore) { /* 失败不阻断登录 */ }
        super.onAuthenticationSuccess(req, resp, auth);
    }
}
