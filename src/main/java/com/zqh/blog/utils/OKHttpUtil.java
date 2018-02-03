package com.zqh.blog.utils;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author 钟启辉
 * @description
 * @company www.jiweitech.com
 * @date 2018-02-01 16:11
 **/
public class OKHttpUtil {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(1000, TimeUnit.SECONDS)
            .build();

    /**
     * 同步执行请求
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    /**
     * 异步执行请求
     * @param request
     * @param callback
     */
    public static void enqueue(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }


    public String getSend(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = execute(request);

        return "";
    }



}
