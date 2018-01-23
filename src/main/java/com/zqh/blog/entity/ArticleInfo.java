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
    /*** 文章主键sn*/
    private Integer sn;
    /*** 文章归类sn*/
    private int categorySn;
    /*** 阅读次数*/
    private int readNum;
    /*** 评论次数*/
    private int reviewNum;
    /*** 赞次数*/
    private int praiseNum;
    /*** 踩次数*/
    private int teaseNum;
    /*** 文章图片url，多个用英文逗号隔开*/
    private String pictureUrl;
    /*** 文章标题*/
    private String aTitle;
    /*** 文章概要*/
    private String aSummary;
    /*** 文章内容*/
    private String aContent;
    /*** 文章插入时间*/
    private String insertTime;
    /*** 文章修改时间*/
    private String updateTime;
}
