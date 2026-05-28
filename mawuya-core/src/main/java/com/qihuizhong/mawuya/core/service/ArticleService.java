/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.common.utils.PageUtil;
import com.qihuizhong.mawuya.common.utils.DateUtil;
import com.qihuizhong.mawuya.core.entity.ArticleInfo;
import com.qihuizhong.mawuya.core.mapper.ArticleInfoMapper;
import com.qihuizhong.mawuya.core.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 文章业务层
 *
 * @author zqh
 */
@Service
public class ArticleService extends BaseService<ArticleInfo, Integer> {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final ArticleInfoMapper articleInfoMapper;

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
     * 封装分页信息：默认每页 10 条
     */
    public Page<ArticleInfo> getPage(HttpServletRequest request) {
        return selectByPage(parseCurr(request), parseLimit(request));
    }

    /**
     * 封装归档信息：key 为年份，value 为文章集合
     */
    public Map<String, List<ArticleInfo>> getYearMap() {
        List<ArticleInfo> infos = articleInfoMapper.selectAllNoContent(new HashMap<>());
        if (infos == null || infos.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<String, List<ArticleInfo>> listMap = new LinkedHashMap<>();
        for (ArticleInfo a : infos) {
            String key = DateUtil.getYear(a.getInsertTime());
            List<ArticleInfo> list = listMap.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(a);
        }
        return listMap;
    }

    public List<ArticleInfo> getAllNoContent(Map<String, ?> map) {
        return articleInfoMapper.selectAllNoContent(map);
    }

    /**
     * 全文搜索（带分页）。
     *
     * <p>关键字会做 SQL LIKE 通配符转义，{@code %} 与 {@code _} 与 {@code \\} 都被转为字面量，
     * 防止用户输入造成查询不可控。同时 keyword 通过 #{} 参数化绑定，无 SQL 注入风险。</p>
     */
    public Page<ArticleInfo> search(String keyword, HttpServletRequest request) {
        int curr = parseCurr(request);
        int limit = parseLimit(request);
        String safeKeyword = escapeLike(keyword);

        int start = (curr - 1) * limit;
        List<ArticleInfo> list = articleInfoMapper.searchByKeyword(safeKeyword, start, limit);
        int total = articleInfoMapper.countByKeyword(safeKeyword);
        int pageSize = (int) Math.ceil(total / (double) limit);

        Page<ArticleInfo> page = new Page<>();
        page.setLists(list)
                .setCurr(curr)
                .setSize(limit)
                .setPageSize(pageSize)
                .setPageLine(PageUtil.pcnDefault(curr, pageSize));
        return page;
    }

    public List<ArticleInfo> listBySnList(List<Integer> sns) {
        if (sns == null || sns.isEmpty()) {
            return new ArrayList<>();
        }
        return articleInfoMapper.selectListBySnList(sns);
    }

    public List<ArticleInfo> getHotTopN(int n) {
        if (n <= 0) {
            return new ArrayList<>();
        }
        return articleInfoMapper.selectHotTopN(n);
    }

    public String delArtcleAndReview(String sn) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            return "fail";
        }

        if (articleInfoMapper.deleteById(id) <= 0) {
            return "fail";
        }
        if (log.isDebugEnabled()) {
            log.debug("delete article: {}", id);
        }
        return "success";
    }

    public int updatePictureUrl(ArticleInfo articleInfo) {
        return articleInfoMapper.updatePictureUrl(articleInfo);
    }

    private int parseCurr(HttpServletRequest request) {
        String currNum = request.getParameter("currNum");
        try {
            if (currNum != null) {
                int v = Integer.parseInt(currNum);
                return v < 1 ? 1 : v;
            }
        } catch (NumberFormatException ignored) {
            // 保留默认值
        }
        return 1;
    }

    private int parseLimit(HttpServletRequest request) {
        String showNum = request.getParameter("showNum");
        try {
            if (showNum != null) {
                int v = Integer.parseInt(showNum);
                return v < 1 ? DEFAULT_PAGE_SIZE : v;
            }
        } catch (NumberFormatException ignored) {
            // 保留默认值
        }
        return DEFAULT_PAGE_SIZE;
    }

    /**
     * 转义 LIKE 通配符（仅 %），避免用户输入用 % 做大范围扫描。
     * <p>{@code _} 不转义：博客搜索场景下用户更可能想匹配"任意字符"而非字面 _，
     * 且 {@code _} 仅是单字符通配，影响极小。</p>
     */
    private String escapeLike(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("%", "");
    }
}
