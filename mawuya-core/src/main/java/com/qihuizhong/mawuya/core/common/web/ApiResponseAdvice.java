/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.common.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qihuizhong.mawuya.core.common.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Method;

/**
 * 统一响应体包装。
 *
 * <p>命中规则（必须全部满足才包装）：</p>
 * <ol>
 *   <li>响应内容类型为 JSON（{@code application/json}）</li>
 *   <li>方法 / 类没有标 {@link SkipApiResponseWrap}</li>
 *   <li>返回值不是 {@link BaseResponse}（避免双重包装）</li>
 *   <li>返回值不是 {@link ResponseEntity}（调用方有更细粒度的 status/header 控制需求）</li>
 *   <li>不是 {@code byte[]} / {@link CharSequence} 之外的特殊类型</li>
 * </ol>
 *
 * <p>basePackages 限定到 {@code com.qihuizhong.mawuya}，避免影响 actuator / springdoc 等三方接口。</p>
 *
 * @author 钟启辉
 */
@RestControllerAdvice(annotations = RestController.class)
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Autowired
    public ApiResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // 排除：方法 / 类显式标注 SkipApiResponseWrap
        Method method = returnType.getMethod();
        if (method == null) return false;
        if (method.isAnnotationPresent(SkipApiResponseWrap.class)) return false;
        Class<?> declaring = method.getDeclaringClass();
        if (declaring.isAnnotationPresent(SkipApiResponseWrap.class)) return false;

        // 排除：返回值已是 BaseResponse / ResponseEntity / byte[]
        Class<?> paramType = returnType.getParameterType();
        if (BaseResponse.class.isAssignableFrom(paramType)) return false;
        if (ResponseEntity.class.isAssignableFrom(paramType)) return false;
        if (byte[].class.equals(paramType)) return false;

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> converterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        // 仅包装 JSON 响应
        if (!isJson(selectedContentType)) {
            return body;
        }

        // String 返回值特殊处理：Spring 走 StringHttpMessageConverter，
        // 直接返回 BaseResponse 实例会 ClassCastException，需手动序列化为 JSON 字符串。
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(BaseResponse.success(body));
            } catch (Exception e) {
                return body;
            }
        }

        if (body instanceof BaseResponse) {
            return body;
        }
        return BaseResponse.success(body);
    }

    private boolean isJson(MediaType selected) {
        if (selected == null) return false;
        return MediaType.APPLICATION_JSON.includes(selected)
                || (selected.getSubtype() != null && selected.getSubtype().endsWith("+json"));
    }
}
