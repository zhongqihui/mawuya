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

    /**
     * 根据map中的key条件，查询不包含内容的 文章集合
     * @param map
     * @return
     */
    List<ArticleInfo> selectAllNoContent(Map map);

    /**
     * 根据这篇文章的主键，查找下一篇文章信息
     * @param id
     * @return
     */
    ArticleInfo selectNextById(Integer id);

    /**
     * 根据这篇文章的主键，查找上一篇文章信息
     * @param id
     * @return
     */
    ArticleInfo selectPrevById(Integer id);

    /**
     * 批量修改sns的readNum + 1
     * @param sns
     * @return
     */
    int updateBatchReadNum(String sns);
}
