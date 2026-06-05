/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.bms.controller.api;

import com.niudeyapi.mawuya.bms.dto.request.SnRequest;
import com.niudeyapi.mawuya.bms.dto.request.UserCreateRequest;
import com.niudeyapi.mawuya.bms.dto.request.UserQueryRequest;
import com.niudeyapi.mawuya.bms.dto.request.UserResetPasswordRequest;
import com.niudeyapi.mawuya.bms.dto.request.UserToggleRequest;
import com.niudeyapi.mawuya.bms.dto.request.UserUpdateRequest;
import com.niudeyapi.mawuya.bms.dto.response.RoleItemResponse;
import com.niudeyapi.mawuya.bms.dto.response.UserCreateResponse;
import com.niudeyapi.mawuya.bms.dto.response.UserItemResponse;
import com.niudeyapi.mawuya.core.common.BaseResponse;
import com.niudeyapi.mawuya.core.common.PageResponse;
import com.niudeyapi.mawuya.core.dataobject.RoleDO;
import com.niudeyapi.mawuya.core.dataobject.UserDO;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.service.SysUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户管理 JSON API。
 *
 * <p>所有方法均要求 ADMIN 角色（类级 {@link PreAuthorize}）。</p>
 *
 * <h3>API 列表</h3>
 * <ul>
 *   <li>{@code GET  /bms/api/user/page}     分页查询</li>
 *   <li>{@code GET  /bms/api/user/roles}    所有可选角色</li>
 *   <li>{@code POST /bms/api/user/create}   创建用户</li>
 *   <li>{@code POST /bms/api/user/update}   更新用户（昵称/邮箱/角色）</li>
 *   <li>{@code POST /bms/api/user/toggle}   启用/禁用</li>
 *   <li>{@code POST /bms/api/user/reset}    重置密码</li>
 *   <li>{@code POST /bms/api/user/delete}   删除（不允许删除内置 admin sn=1）</li>
 * </ul>
 *
 * @author 钟启辉
 */
@RestController
@RequestMapping("bms/api/user")
@PreAuthorize("hasRole('ADMIN')")
public class UserApiController {

    @Autowired private SysUserService  sysUserService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("page")
    public BaseResponse<PageResponse<UserItemResponse>> page(@Valid UserQueryRequest req) {
        int total = sysUserService.count(req.getKeyword());
        List<UserDO> list = sysUserService.listByPage(req.getKeyword(), req.getPage(), req.getSize());
        List<UserItemResponse> dtos = list == null ? Collections.emptyList()
                : list.stream().map(UserApiController::toUserItem).collect(Collectors.toList());
        return BaseResponse.success(PageResponse.of(dtos, total, req.getPage(), req.getSize()));
    }

    @GetMapping("roles")
    public BaseResponse<List<RoleItemResponse>> roles() {
        List<RoleDO> roles = sysUserService.listAllRoles();
        List<RoleItemResponse> dtos = roles == null ? Collections.emptyList()
                : roles.stream().map(UserApiController::toRoleItem).collect(Collectors.toList());
        return BaseResponse.success(dtos);
    }

    @PostMapping("create")
    public BaseResponse<UserCreateResponse> create(@Valid UserCreateRequest req) {
        UserDO u = new UserDO()
                .setUsername(req.getUsername().trim())
                .setPasswordHash(passwordEncoder.encode(req.getPassword()))
                .setNickname(StringUtils.isBlank(req.getNickname()) ? null : req.getNickname().trim())
                .setEmail(StringUtils.isBlank(req.getEmail()) ? null : req.getEmail().trim())
                .setEnabled(req.getEnabled() == null || req.getEnabled() != 0 ? 1 : 0);
        int sn = sysUserService.save(u, parseCodes(req.getRoleCodes()));
        return BaseResponse.success("创建成功", new UserCreateResponse(sn));
    }

    @PostMapping("update")
    public BaseResponse<Void> update(@Valid UserUpdateRequest req) {
        UserDO u = new UserDO()
                .setSn(req.getSn())
                .setNickname(StringUtils.isBlank(req.getNickname()) ? null : req.getNickname().trim())
                .setEmail(StringUtils.isBlank(req.getEmail()) ? null : req.getEmail().trim())
                .setEnabled(req.getEnabled());
        sysUserService.updateById(u, parseCodes(req.getRoleCodes()));
        return BaseResponse.success("更新成功", null);
    }

    @PostMapping("toggle")
    public BaseResponse<Void> toggle(@Valid UserToggleRequest req) {
        sysUserService.updateEnabledById(req.getSn(), req.getEnabled() != null && req.getEnabled() == 1);
        return BaseResponse.success(req.getEnabled() == 1 ? "已启用" : "已禁用", null);
    }

    @PostMapping("reset")
    public BaseResponse<Void> reset(@Valid UserResetPasswordRequest req) {
        sysUserService.updatePasswordHashById(req.getSn(), passwordEncoder.encode(req.getPassword()));
        return BaseResponse.success("密码已重置", null);
    }

    @PostMapping("delete")
    public BaseResponse<Void> delete(@Valid SnRequest req) {
        // 不允许删除内置 admin（sn=1）
        if (req.getSn() != null && req.getSn() == 1) {
            throw new BusinessException("不允许删除内置管理员");
        }
        sysUserService.removeById(req.getSn());
        return BaseResponse.success("已删除", null);
    }

    // ---------------- helpers ----------------

    private static UserItemResponse toUserItem(UserDO u) {
        return new UserItemResponse()
                .setSn(u.getSn())
                .setUsername(u.getUsername())
                .setNickname(u.getNickname())
                .setEmail(u.getEmail())
                .setEnabled(u.getEnabled())
                .setRoleCodes(u.getRoleCodes())
                .setCreatedAt(u.getCreatedAt())
                .setLastLoginAt(u.getLastLoginAt());
    }

    private static RoleItemResponse toRoleItem(RoleDO r) {
        return new RoleItemResponse()
                .setSn(r.getSn())
                .setCode(r.getCode())
                .setName(r.getName())
                .setDescription(r.getDescription());
    }

    /** 把逗号分隔字符串解析成 code 列表；null/空返回 null（表示不修改绑定） */
    private static List<String> parseCodes(String roleCodes) {
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
