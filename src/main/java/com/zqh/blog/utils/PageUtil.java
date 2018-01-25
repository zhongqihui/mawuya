package com.zqh.blog.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 钟启辉
 * @description
 * @company www.jiweitech.com
 * @date 2018-01-25 15:18
 **/
public class PageUtil {

    /**
     * 默认显示5页
     * @param currNum
     * @param pageSize
     * @return
     */
    public static List<String> pcnDefault(int currNum, int pageSize) {
        return pcn(currNum, pageSize, 5);
    }


    /**
     * @param currNum 第几页 ，如：第1页
     * @param pageSize 总共几页 ，如：共10页
     * @param showNum 前台显示几页，如：4页
     * @return 如：1,2,3...10
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


    public static void main(String[] args) {
        List<String> strings = PageUtil.pcnDefault(1, 7);
        System.out.println(strings);
    }
}
