/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.mawuya.common.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 通用调用工具。
 * <p>仅提供基础的同步/异步执行能力；具体业务回调（如淘宝 IP 接口）由调用方实现，
 * 避免本工具类反向依赖业务模块。</p>
 *
 * @author zqh
 */
public class OkHttpUtil {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private OkHttpUtil() {
    }

    public static OkHttpClient client() {
        return CLIENT;
    }

    /**
     * 同步执行请求
     */
    public static Response execute(Request request) throws IOException {
        return CLIENT.newCall(request).execute();
    }

    /**
     * 异步执行请求
     */
    public static void enqueue(Request request, Callback callback) {
        CLIENT.newCall(request).enqueue(callback);
    }
}
