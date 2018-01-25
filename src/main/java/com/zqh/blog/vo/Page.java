package com.zqh.blog.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/23 10:29
 * description: 分页查询bean
 **/
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Page<T> implements Serializable {

    /**
     * 总页数
     */
    private int pageSize;

    /**
     * 当前页数
     */
    private int curr;

    /**
     * 给前台展示的页码轴
     */
    private List<String> pageLine;

    /**
     * 每页展示条数
     */
    private int size;

    /**
     * 响应路径，供前台分页
     */
    private String url;

    /**
     * 查询的记录
     */
    private List<T> lists;
}
