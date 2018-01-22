package com.zqh.blog.service;

import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.mapper.ArticleInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/1/21 0021 下午 9:13
 * description: 文章管理的业务层
 **/
@Service
public class ArticleService extends BaseService<ArticleInfo, Integer> {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);


    @Autowired
    ArticleInfoMapper articleInfoMapper;

    public List<ArticleInfo> getArticles() {
        Map<String, Integer> map = new HashMap<>();
        map.put("start", 0);
        map.put("limit", 1);
        List<ArticleInfo> articleInfos = articleInfoMapper.selectAllNoContent(map);
        return articleInfos;
    }
}
