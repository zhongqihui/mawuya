package com.zqh.blog.utils;

import org.apache.commons.lang.StringUtils;

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
}
