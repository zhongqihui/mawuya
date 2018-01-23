package com.zqh.blog.mapper;

import com.zqh.blog.entity.ArticleInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
* @author 钟启辉
* @company www.jiweitech.com
* @date 2018/1/18 11:40
* @description 文章dao层
**/
@Repository
public interface ArticleInfoMapper extends BaseMapper<ArticleInfo, Integer> {

    List<ArticleInfo> selectAllNoContent(Map map);
}
