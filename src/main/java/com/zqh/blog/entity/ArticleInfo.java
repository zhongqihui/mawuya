package com.zqh.blog.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author 钟启辉
 * @description 文章实体
 * @company www.jiweitech.com
 * @date 2018-01-18 11:19
 **/
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class ArticleInfo implements Serializable {
    private Integer sn;
    private String categorySn;
    private int readNum;
    private int reviewNum;
    private int praiseNum;
    private int teaseNum;
    private String pictureUrl;
    private String aTitle;
    private String aSummary;
    private String aContent;
    private String insertTime;
    private String updateTime;
}
