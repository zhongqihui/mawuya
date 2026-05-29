/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import com.qihuizhong.mawuya.core.entity.SysRole;
import com.qihuizhong.mawuya.core.entity.SysUser;
import com.qihuizhong.mawuya.core.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BMS 系统用户管理。
 *
 * <p>所有方法均要求 ADMIN 角色（类级 {@link PreAuthorize}）。</p>
 *
 * <p>路由：</p>
 * <ul>
 *   <li>{@code GET /bms/user/list.do}      列表页</li>
 *   <li>{@code GET /bms/user/page.do}      AJAX 分页查询</li>
 *   <li>{@code POST /bms/user/create.do}   创建用户（含角色）</li>
 *   <li>{@code POST /bms/user/update.do}   更新用户（昵称/邮箱/角色）</li>
 *   <li>{@code POST /bms/user/toggle.do}   启用/禁用</li>
 *   <li>{@code POST /bms/user/reset.do}    重置密码</li>
 *   <li>{@code POST /bms/user/delete.do}   删除</li>
 *   <li>{@code GET  /bms/user/roles.do}    所有可选角色</li>
 * </ul>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/user")
@PreAuthorize("hasRole('ADMIN')")
public class SysUserController extends BaseController {

    @Autowired private SysUserService  sysUserService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("list.do")
    public String list(Model model) {
        return "bms/user/user_list";
    }

    @GetMapping("page.do")
    @ResponseBody
    public Map<String, Object> page(@RequestParam(value = "keyword", required = false) String keyword,
                                    @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {
        int total = sysUserService.count(keyword);
        List<SysUser> list = sysUserService.page(keyword, page, size);
        int safePage = Math.max(1, page);
        int safeSize = (size < 1 || size > 200) ? 20 : size;
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", 1);
        res.put("list",  list);
        res.put("total", total);
        res.put("page",  safePage);
        res.put("size",  safeSize);
        res.put("pages", (total + safeSize - 1) / safeSize);
        return res;
    }

    @GetMapping("roles.do")
    @ResponseBody
    public Map<String, Object> roles() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", 1);
        res.put("list", sysUserService.listAllRoles());
        return res;
    }

    @PostMapping("create.do")
    @ResponseBody
    public Map<String, Object> create(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      @RequestParam(value = "nickname", required = false) String nickname,
                                      @RequestParam(value = "email", required = false) String email,
                                      @RequestParam(value = "enabled", required = false, defaultValue = "1") Integer enabled,
                                      @RequestParam(value = "roleCodes", required = false) String roleCodes) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                throw new IllegalArgumentException("用户名/密码不能为空");
            }
            if (password.length() < 6) {
                throw new IllegalArgumentException("密码至少 6 位");
            }
            SysUser u = new SysUser()
                    .setUsername(username.trim())
                    .setPasswordHash(passwordEncoder.encode(password))
                    .setNickname(StringUtils.isBlank(nickname) ? null : nickname.trim())
                    .setEmail(StringUtils.isBlank(email) ? null : email.trim())
                    .setEnabled(enabled == null || enabled != 0 ? 1 : 0);
            int sn = sysUserService.create(u, parseCodes(roleCodes));
            res.put("success", 1);
            res.put("sn", sn);
            res.put("message", "创建成功");
        } catch (IllegalArgumentException e) {
            res.put("success", 0);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("update.do")
    @ResponseBody
    public Map<String, Object> update(@RequestParam("sn") Integer sn,
                                      @RequestParam(value = "nickname", required = false) String nickname,
                                      @RequestParam(value = "email", required = false) String email,
                                      @RequestParam(value = "enabled", required = false) Integer enabled,
                                      @RequestParam(value = "roleCodes", required = false) String roleCodes) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            SysUser u = new SysUser()
                    .setSn(sn)
                    .setNickname(StringUtils.isBlank(nickname) ? null : nickname.trim())
                    .setEmail(StringUtils.isBlank(email) ? null : email.trim())
                    .setEnabled(enabled);
            sysUserService.update(u, parseCodes(roleCodes));
            res.put("success", 1);
            res.put("message", "更新成功");
        } catch (IllegalArgumentException e) {
            res.put("success", 0);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("toggle.do")
    @ResponseBody
    public Map<String, Object> toggle(@RequestParam("sn") Integer sn,
                                      @RequestParam("enabled") Integer enabled) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            sysUserService.setEnabled(sn, enabled != null && enabled == 1);
            res.put("success", 1);
            res.put("message", enabled == 1 ? "已启用" : "已禁用");
        } catch (IllegalArgumentException e) {
            res.put("success", 0);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("reset.do")
    @ResponseBody
    public Map<String, Object> reset(@RequestParam("sn") Integer sn,
                                     @RequestParam("password") String password) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            if (StringUtils.isBlank(password) || password.length() < 6) {
                throw new IllegalArgumentException("密码至少 6 位");
            }
            sysUserService.resetPasswordHash(sn, passwordEncoder.encode(password));
            res.put("success", 1);
            res.put("message", "密码已重置");
        } catch (IllegalArgumentException e) {
            res.put("success", 0);
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping("delete.do")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("sn") Integer sn) {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            // 不允许删除内置 admin（sn=1）
            if (sn != null && sn == 1) {
                throw new IllegalArgumentException("不允许删除内置管理员");
            }
            sysUserService.delete(sn);
            res.put("success", 1);
            res.put("message", "已删除");
        } catch (IllegalArgumentException e) {
            res.put("success", 0);
            res.put("message", e.getMessage());
        }
        return res;
    }

    /** 把逗号分隔字符串解析成 code 列表；null/空返回 null（表示不修改绑定） */
    private List<String> parseCodes(String roleCodes) {
        if (roleCodes == null) return null;
        String t = roleCodes.trim();
        if (t.isEmpty()) return Collections.emptyList(); // 空串表示清空角色
        return Arrays.stream(t.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }
}
