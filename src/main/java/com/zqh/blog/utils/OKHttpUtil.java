package com.zqh.blog.utils;

import com.zqh.blog.cache.DataCenter;
import com.zqh.blog.entity.LogInfo;
import com.zqh.blog.interceptor.LogInterceptor;
import net.sf.json.JSONObject;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/7 14:20
 * description: okhttp请求工具
 **/
public class OKHttpUtil {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(1000, TimeUnit.SECONDS)
            .readTimeout(1000, TimeUnit.SECONDS)
            .writeTimeout(1000, TimeUnit.SECONDS)
            .build();

    /**
     * 同步执行请求
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    /**
     * 异步执行请求
     *
     * @param request
     * @param callback
     */
    public static void enqueue(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }


    /**
     * 淘宝ip查询接口
     *
     * @param info
     */
    public static void taobaoSend(LogInfo info) {
        if (StringUtils.isEmpty(info.getIpAddr())) {
            return;
        }

        String url = "http://ip.taobao.com/service/getIpInfo.php?ip=" + info.getIpAddr();
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/4.0")
                .addHeader("Accept", "application/json")
                .get()
                .build();

        enqueue(request, new TaoBaoCallBack(info));
    }

}

class TaoBaoCallBack implements Callback {
    private static final Logger log = LoggerFactory.getLogger(LogInterceptor.class);
    private LogInfo logInfo;

    TaoBaoCallBack(LogInfo logInfo) {
        if(logInfo == null) {
            this.logInfo = new LogInfo();
        }

        this.logInfo = logInfo;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        try{
            if(logInfo.getTryTimes() >= 3) {
                DataCenter.getLogInfoToDBQueue().put(logInfo);
            }else {
                DataCenter.getLogInfoToAPIQueue().put(logInfo);
                if(log.isWarnEnabled()) {
                    log.warn("访客ip：" + logInfo.getIpAddr() + "向淘宝api推送3次后，仍然失败后入库");
                }
            }
        }catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        JSONObject json = JSONObject.fromObject(response.body().string());

        if("0".equals(json.getString("code"))) {
            JSONObject data = json.getJSONObject("data");
            logInfo.setCountry(data.getString("country"))
                    .setProvince(data.getString("region"))
                    .setCity(data.getString("city"))
                    .setIsp(data.getString("isp"));
        }

        try {
            DataCenter.getLogInfoToDBQueue().put(logInfo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
