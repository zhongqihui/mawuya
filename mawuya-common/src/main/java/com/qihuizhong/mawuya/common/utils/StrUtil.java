/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.common.utils;

/**
 * 字符串工具类。
 * <p>该类的方法被 MyBatis Mapper XML 中的 OGNL 表达式以全限定类名调用：
 * {@code @com.qihuizhong.mawuya.common.utils.StrUtil@isAllNoEmpty(start,limit)}，
 * 修改包路径或方法名时务必同步更新所有 mapper.xml。</p>
 *
 * @author zqh
 */
public class StrUtil {

    private StrUtil() {
    }

    /**
     * 传入的参数全部为 null 或空串时返回 true。
     */
    public static boolean isAllEmpty(Object... os) {
        if (os == null) {
            return true;
        }
        for (Object o : os) {
            if (o != null && !"".equals(o.toString())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 传入的参数全部不为 null 或空串时返回 true。
     */
    public static boolean isAllNoEmpty(Object... os) {
        if (os == null || os.length == 0) {
            return false;
        }
        for (Object o : os) {
            if (o == null || "".equals(o.toString())) {
                return false;
            }
        }
        return true;
    }
}
