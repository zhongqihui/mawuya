/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * BMS 登录成功响应 DTO。
 *
 * <p>结构稳定：</p>
 * <pre>
 *   {
 *     "token": "eyJhbGciOi...",
 *     "user": { "sn":1, "username":"admin", "nickname":"...", "roles":["ADMIN"] }
 *   }
 * </pre>
 *
 * @author 钟启辉
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String token;
    private UserBrief user;

    public LoginResponse() {}

    public LoginResponse(String token, UserBrief user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public LoginResponse setToken(String token) { this.token = token; return this; }

    public UserBrief getUser() { return user; }
    public LoginResponse setUser(UserBrief user) { this.user = user; return this; }

    /** 当前用户简要信息（用于登录响应 / 当前会话查询响应） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserBrief {
        private Integer sn;
        private String  username;
        private String  nickname;
        private List<String> roles;

        public UserBrief() {}

        public UserBrief(Integer sn, String username, String nickname, List<String> roles) {
            this.sn = sn;
            this.username = username;
            this.nickname = nickname;
            this.roles = roles;
        }

        public Integer getSn() { return sn; }
        public UserBrief setSn(Integer sn) { this.sn = sn; return this; }

        public String getUsername() { return username; }
        public UserBrief setUsername(String username) { this.username = username; return this; }

        public String getNickname() { return nickname; }
        public UserBrief setNickname(String nickname) { this.nickname = nickname; return this; }

        public List<String> getRoles() { return roles; }
        public UserBrief setRoles(List<String> roles) { this.roles = roles; return this; }
    }
}
