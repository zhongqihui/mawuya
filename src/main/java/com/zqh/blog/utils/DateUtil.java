package com.zqh.blog.utils;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/7 18:14
 * description: 日期工具类
 **/
public class DateUtil {

    /**
     * 截取时间字符串的年，如：2018-02-07 18:30:00 》》 2018
     * @param date
     * @return
     */
    public static String getYear(String date) {
        return StringUtils.isNotEmpty(date) && date.length() >= 4 ? date.substring(0, 4) : "";
    }


    /**
     * 获取当前时间字符串，如：2018-02-07 18:30:00
     * @return
     */
    public static String getNowStr() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 时间毫秒数，转字符串，如：System.currentTimeMillis() 》》 2018-02-07 18:30:00
     * @param l
     * @return
     */
    public static String long2Str(Long l) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(l));
    }
}
