/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.bms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    /** HS256 header 是固定的 */
    private static final String HEADER_JSON_B64 = URL_ENC.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

    @Value("${mawuya.security.jwt.secret:CHANGE_ME_PLEASE_USE_AT_LEAST_32_BYTES_secret}")
    private String secret;

    /** 默认 8 小时 */
    @Value("${mawuya.security.jwt.expires-seconds:28800}")
    private long expiresSeconds;

    /** 签发 token */
    public String issue(int userSn, String username, List<String> roles) {
        long now = System.currentTimeMillis() / 1000L;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub",   userSn);
        payload.put("uname", username);
        payload.put("roles", roles == null ? java.util.Collections.emptyList() : roles);
        payload.put("iat",   now);
        payload.put("exp",   now + expiresSeconds);

        try {
            String payloadJson = MAPPER.writeValueAsString(payload);
            String payloadB64 = URL_ENC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signingInput = HEADER_JSON_B64 + "." + payloadB64;
            String sig = sign(signingInput);
            return signingInput + "." + sig;
        } catch (Exception e) {
            throw new RuntimeException("issue jwt failed: " + e.getMessage(), e);
        }
    }

    /**
     * 解析并校验 token：签名错误 / 过期 / 格式错误均返回 null（不抛异常）。
     */
    public JwtPayload parse(String token) {
        if (token == null || token.isEmpty()) return null;
        // 兼容 Bearer 前缀
        if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        // 校验 header
        if (!HEADER_JSON_B64.equals(parts[0])) {
            // header 不一致也直接拒绝
            return null;
        }
        try {
            // 校验签名
            String expected = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expected, parts[2])) {
                return null;
            }
            byte[] payloadBytes = URL_DEC.decode(parts[1]);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = MAPPER.readValue(payloadBytes, Map.class);

            long exp = ((Number) map.getOrDefault("exp", 0)).longValue();
            if (exp > 0 && exp < System.currentTimeMillis() / 1000L) {
                return null; // 过期
            }
            JwtPayload p = new JwtPayload();
            Object sub = map.get("sub");
            if (sub instanceof Number) p.userSn = ((Number) sub).intValue();
            p.username = (String) map.get("uname");
            Object rolesObj = map.get("roles");
            if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> raw = (List<Object>) rolesObj;
                List<String> rs = new java.util.ArrayList<>(raw.size());
                for (Object o : raw) if (o != null) rs.add(o.toString());
                p.roles = rs;
            } else {
                p.roles = java.util.Collections.emptyList();
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
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return URL_ENC.encodeToString(raw);
    }

    /** 常量时间比较，避免计时旁路 */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    /** 解析后的 payload 简单结构（公开字段以减少 getter 噪声） */
    public static class JwtPayload {
        public int          userSn;
        public String       username;
        public List<String> roles;
        public long         exp;
    }
}
