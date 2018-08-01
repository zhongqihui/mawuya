package com.zqh.blog.entity;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
* author: zqh
* email：zqhfsf@gmail.com
* date: 2018/1/25 0025 下午 10:00
* description: 博客分类
**/
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Accessors(chain = true)
@ToString
public class Category {
    private Integer sn;
    @NonNull private String categoryName;
    private List<ArticleInfo> arts;
    private int artSize;
}
