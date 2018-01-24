package com.zqh.blog.service;

import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.mapper.ArticleInfoMapper;
import com.zqh.blog.mapper.BaseMapper;
import com.zqh.blog.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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

    ArticleInfoMapper articleInfoMapper;

    @Autowired
    public ArticleService(ArticleInfoMapper baseMapper) {
        super(baseMapper);
        this.articleInfoMapper = baseMapper;
    }

    public List<ArticleInfo> getArticles() {
        Map<String, Integer> map = new HashMap<>();
        map.put("start", 0);
        map.put("limit", 1);
        List<ArticleInfo> articleInfos = articleInfoMapper.selectAllNoContent(map);
        return articleInfos;
    }

    public ArticleInfo getNext(Integer aid) {
        return articleInfoMapper.selectNextById(aid);
    }

    public ArticleInfo getPrev(Integer aid) {
        return articleInfoMapper.selectPrevById(aid);
    }

    public Page<ArticleInfo> getPage(HttpServletRequest request) {
        String currNum = request.getParameter("currNum");
        String showNum = request.getParameter("showNum");
        Integer curr = 1;Integer limit = 1;
        try{
            curr = Integer.parseInt(currNum);
            limit = Integer.parseInt(showNum);
        }catch (Exception ignored) {}

        return selectByPage(curr, limit);
    }
}
