package com.zqh.blog.service;

import com.zqh.blog.entity.ArticleInfo;
import com.zqh.blog.mapper.ArticleInfoMapper;
import com.zqh.blog.mapper.BaseMapper;
import com.zqh.blog.utils.DateUtil;
import com.zqh.blog.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

    public ArticleInfo getNext(Integer aid) {
        return articleInfoMapper.selectNextById(aid);
    }

    public ArticleInfo getPrev(Integer aid) {
        return articleInfoMapper.selectPrevById(aid);
    }

    /**
     * 封装分页信息
     * @param request
     * @return
     */
    public Page<ArticleInfo> getPage(HttpServletRequest request) {
        String currNum = request.getParameter("currNum");
        String showNum = request.getParameter("showNum");
        Integer curr = 1;Integer limit = 2;
        try{
            curr = Integer.parseInt(currNum);
            limit = Integer.parseInt(showNum);
        }catch (Exception ignored) {}

        return selectByPage(curr, limit);
    }

    /**
     * 封装归档信息
     * @return key为年份，value为Article集合
     */
    public Map<String,List<ArticleInfo>> getYearMap() {
        List<ArticleInfo> infos = articleInfoMapper.selectAllNoContent(new HashMap<>());
        if(infos == null || infos.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<ArticleInfo>> listMap = new LinkedHashMap<>();
        for (ArticleInfo a : infos) {
            String key = DateUtil.getYear(a.getInsertTime());
            if(!listMap.containsKey(key)) {
                List<ArticleInfo> list = new ArrayList<>();
                list.add(a);
                listMap.put(key, list);
                continue;
            }

            List<ArticleInfo> list = listMap.get(key);
            list.add(a);
        }

        return listMap;
    }
}
