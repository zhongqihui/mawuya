package com.zqh.blog.vo;

import com.zqh.blog.entity.ArticleInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/23 10:47
 * description: 文章vo 与视图层相关
 **/
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class ArticleVo extends Page<ArticleInfo> {
    private ArticleInfo article;
}
