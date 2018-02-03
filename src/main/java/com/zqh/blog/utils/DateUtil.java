package com.zqh.blog.utils;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 钟启辉
 * @description
 * @company www.jiweitech.com
 * @date 2018-01-25 18:21
 **/
public class DateUtil {

    public static String getYear(String date) {
        return StringUtils.isNotEmpty(date) && date.length() >= 4 ? date.substring(0, 4) : "";
    }

    public static String getNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
