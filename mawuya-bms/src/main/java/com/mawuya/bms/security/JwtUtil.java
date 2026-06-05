/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.bms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mawuya.core.enums.ResultCodeEnum;
import com.mawuya.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极简 JWT 工具：自实现 HMAC-SHA256 签名（HS256），不引入 jjwt 等三方库。
 *
 * <p>Token 结构：base64url(header).base64url(payload).base64url(sig)；payload 字段：</p>
 * <ul>
 *   <li>{@code sub}   —— 用户 sn（数字）</li>
 *   <li>{@code uname} —— 用户名</li>
 *   <li>{@code roles} —— 角色 code 列表</li>
 *   <li>{@code iat}   —— 签发时间（秒）</li>
 *   <li>{@code exp}   —— 过期时间（秒）</li>
 * </ul>
 *
 * <p>密钥来自 application.yml 的 {@code mawuya.security.jwt.secret}；
 * 默认值为占位符，<b>生产部署必须覆盖</b>。</p>
 *
 * @author 钟启辉
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Base64.Encoder URL_ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DEC = Base64.getUrlDecoder();

    /** HS256 算法名 */
    private static final String HMAC_SHA256 = "HmacSHA256";
    /** Bearer token 前缀（含尾随空格） */
    private static final String BEARER_PREFIX = "Bearer ";
    /** 期望分段数 */
    private static final int JWT_SEGMENTS = 3;
    /** 签名输入分隔符 */
    private static final String DOT = ".";

    /** HS256 header 是固定的 */
    private static final String HEADER_JSON_B64 = URL_ENC.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    /** Payload Map 初始容量（5 个固定 key） */
    private static final int PAYLOAD_CAPACITY = 8;

    @Value("${mawuya.security.jwt.secret:CHANGE_ME_PLEASE_USE_AT_LEAST_32_BYTES_secret}")
    private String secret;

    /** 默认 8 小时（28800 秒） */
    @Value("${mawuya.security.jwt.expires-seconds:28800}")
    private long expiresSeconds;

    /** 签发 token */
    public String issue(int userSn, String username, List<String> roles) {
        long now = System.currentTimeMillis() / 1000L;
        Map<String, Object> payload = new LinkedHashMap<>(PAYLOAD_CAPACITY);
        payload.put("sub",   userSn);
        payload.put("uname", username);
        payload.put("roles", roles == null ? Collections.emptyList() : roles);
        payload.put("iat",   now);
        payload.put("exp",   now + expiresSeconds);

        try {
            String payloadJson = MAPPER.writeValueAsString(payload);
            String payloadB64 = URL_ENC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signingInput = HEADER_JSON_B64 + DOT + payloadB64;
            String sig = sign(signingInput);
            return signingInput + DOT + sig;
        } catch (Exception e) {
            // 阿里规约：禁止裸 throw new RuntimeException；改抛业务异常携带错误码 B0001
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR, "JWT 签发失败：" + e.getMessage());
        }
    }

    /**
     * 解析并校验 token：签名错误 / 过期 / 格式错误均返回 null（不抛异常）。
     */
    public JwtPayload parse(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 兼容 Bearer 前缀
        String raw = token;
        if (raw.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            raw = raw.substring(BEARER_PREFIX.length()).trim();
        }
        String[] parts = raw.split("\\.");
        if (parts.length != JWT_SEGMENTS) {
            return null;
        }
        // 校验 header
        if (!HEADER_JSON_B64.equals(parts[0])) {
            return null;
        }
        try {
            // 校验签名
            String expected = sign(parts[0] + DOT + parts[1]);
            if (!constantTimeEquals(expected, parts[2])) {
                return null;
            }
            byte[] payloadBytes = URL_DEC.decode(parts[1]);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = MAPPER.readValue(payloadBytes, Map.class);

            long exp = ((Number) map.getOrDefault("exp", 0)).longValue();
            if (exp > 0 && exp < System.currentTimeMillis() / 1000L) {
                // 过期
                return null;
            }
            JwtPayload p = new JwtPayload();
            Object sub = map.get("sub");
            if (sub instanceof Number) {
                p.userSn = ((Number) sub).intValue();
            }
            p.username = (String) map.get("uname");
            Object rolesObj = map.get("roles");
            if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> rawRoles = (List<Object>) rolesObj;
                List<String> rs = new ArrayList<>(rawRoles.size());
                for (Object o : rawRoles) {
                    if (o != null) {
                        rs.add(o.toString());
                    }
                }
                p.roles = rs;
            } else {
                p.roles = Collections.emptyList();
            }
            p.exp = exp;
            return p;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("jwt parse failed: {}", e.getMessage());
            }
            return null;
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return URL_ENC.encodeToString(raw);
    }

    /** 常量时间比较，避免计时旁路。 */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    /**
     * 解析后的 payload 简单结构。
     *
     * <p>历史代码中的 public 字段沿用，但同时提供 getter，鼓励调用方走 getter（阿里规约：
     * 类成员变量必须有访问控制，不可直接暴露 public 非常量字段）。后续可在迁移完成后将字段改为 private。</p>
     */
    public static class JwtPayload {
        /** 用户 sn */
        public int userSn;
        /** 用户名 */
        public String username;
        /** 角色 code 列表 */
        public List<String> roles;
        /** 过期时间（epoch 秒） */
        public long exp;

        public int getUserSn() {
            return userSn;
        }

        public String getUsername() {
            return username;
        }

        public List<String> getRoles() {
            return roles;
        }

        public long getExp() {
            return exp;
        }

        @Override
        public String toString() {
            return "JwtPayload{userSn=" + userSn + ", username='" + username
                    + "', roles=" + roles + ", exp=" + exp + '}';
        }
    }
}
