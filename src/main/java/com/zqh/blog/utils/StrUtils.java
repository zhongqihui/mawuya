package com.zqh.blog.utils;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/1/22 0022 下午 8:18
* description: String 工具类
**/
public class StrUtils {

    /**
     *  mybatis 校验工具
     * @param os
     * @return 传入的参数都为null或“”，返回true，否则返回false
     */
    public static boolean isAllEmpty(Object...os) {
        for (Object o : os) {
            if(o != null && !"".equals(o.toString())) {
                return false;
            }
        }

        return true;
    }

    /**
     * mybatis 校验工具
     * @param os
     * @return 传入的参数都不为null或者“”，返回true，否则返回false
     */
    public static boolean isAllNoEmpty(Object...os) {
        for(Object o: os) {
            if(o == null && "".equals(o)) {
                return false;
            }
        }

        return true;
    }
}
