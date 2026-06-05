/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页页码渲染工具类
 *
 * @author zqh
 */
public class PageUtil {

    private PageUtil() {
    }

    /**
     * 默认显示 5 页
     */
    public static List<String> pcnDefault(int currNum, int pageSize) {
        return pcn(currNum, pageSize, 5);
    }

    /**
     * @param currNum  第几页
     * @param pageSize 总共几页
     * @param showNum  前台显示几页
     * @return 形如：1,2,3,...,10
     */
    public static List<String> pcn(int currNum, int pageSize, int showNum) {
        List<String> list = new ArrayList<>();
        list.add("1");
        showNum = showNum - 2;
        int start = Math.round(currNum - showNum / 2);
        int end = Math.round(currNum + showNum / 2);

        if (start <= 1) {
            start = 2;
            end = start + showNum - 1;
            if (end >= pageSize - 1) {
                end = pageSize - 1;
            }
        }

        if (end >= pageSize - 1) {
            end = pageSize - 1;
            start = end - showNum + 1;
            if (start <= 1) {
                start = 2;
            }
        }

        if (start != 2) {
            list.add("...");
        }

        for (int i = start; i <= end; i++) {
            list.add("" + i);
        }

        if (end != pageSize - 1) {
            list.add("...");
        }

        list.add("" + pageSize);
        return list;
    }
}
