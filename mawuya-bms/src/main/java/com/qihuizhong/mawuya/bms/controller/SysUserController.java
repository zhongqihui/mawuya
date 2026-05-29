/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * BMS 系统用户管理视图 controller。
 *
 * <p>JSON 操作（增删改查 / 启停 / 重置密码 / 角色查询）已迁移至
 * {@link com.qihuizhong.mawuya.bms.controller.api.UserApiController}。</p>
 *
 * @author 钟启辉
 */
@Controller
@RequestMapping("bms/user")
@PreAuthorize("hasRole('ADMIN')")
public class SysUserController extends BaseController {

    @GetMapping("list.do")
    public String list() {
        return "bms/user/user_list";
    }
}
