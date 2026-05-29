/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.core.service;

import com.qihuizhong.mawuya.common.utils.DateUtil;
import com.qihuizhong.mawuya.common.utils.PageUtil;
import com.qihuizhong.mawuya.core.dataobject.ArticleDO;
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
 * 文章业务层。
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code get/list/count/save/update/remove/search}。</p>
 *
 * @author 钟启辉
 */
@Service
public class ArticleService extends BaseService<ArticleDO, Integer> {

    private static final Logger log = LoggerFactory.getLogger(ArticleService.class);
    /** 默认每页条数 */
    private static final int DEFAULT_PAGE_SIZE = 10;
    /** 空查询条件 Map 的初始容量 */
    private static final int EMPTY_CONDITION_CAPACITY = 4;
    /** 年度归档 Map 初始容量（按年份计，10 年以内常态） */
    private static final int YEAR_MAP_CAPACITY = 16;
    /** 单年文章 List 初始容量 */
    private static final int YEAR_LIST_CAPACITY = 16;
    /** 业务状态字符串：成功 */
    public static final String OPS_SUCCESS = "success";
    /** 业务状态字符串：失败 */
    public static final String OPS_FAIL    = "fail";

    private final ArticleInfoMapper articleInfoMapper;

    @Autowired
    public ArticleService(ArticleInfoMapper baseMapper) {
        super(baseMapper);
        this.articleInfoMapper = baseMapper;
    }

    /** 取下一篇文章。 */
    public ArticleDO getNextById(Integer aid) {
        return articleInfoMapper.selectNextById(aid);
    }

    /** 取上一篇文章。 */
    public ArticleDO getPrevById(Integer aid) {
        return articleInfoMapper.selectPrevById(aid);
    }

    /** 从 HttpServletRequest 解析分页参数后查询（默认 10 条/页）。 */
    public Page<ArticleDO> listByPageFromRequest(HttpServletRequest request) {
        return listByPage(parseCurr(request), parseLimit(request));
    }

    /** 按年份分组的归档 map：key 为年份，value 为该年文章集合。 */
    public Map<String, List<ArticleDO>> listGroupByYear() {
        List<ArticleDO> infos = articleInfoMapper.selectAllNoContent(new HashMap<>(EMPTY_CONDITION_CAPACITY));
        if (infos == null || infos.isEmpty()) {
            return new LinkedHashMap<>(YEAR_MAP_CAPACITY);
        }

        Map<String, List<ArticleDO>> listMap = new LinkedHashMap<>(YEAR_MAP_CAPACITY);
        for (ArticleDO a : infos) {
            String key = DateUtil.getYear(a.getInsertTime());
            List<ArticleDO> list = listMap.computeIfAbsent(key, k -> new ArrayList<>(YEAR_LIST_CAPACITY));
            list.add(a);
        }
        return listMap;
    }

    /** 列出（不含 articleContent 大字段）所有文章，用于归档/列表/sitemap 等。 */
    public List<ArticleDO> listAllNoContent(Map<String, ?> map) {
        return articleInfoMapper.selectAllNoContent(map);
    }

    /**
     * 关键字全文搜索（带分页）。
     *
     * <p>关键字会做 SQL LIKE 通配符转义，{@code %} 被去除避免大范围扫描；
     * 同时 keyword 通过 {@code #{}} 参数化绑定，无 SQL 注入风险。</p>
     */
    public Page<ArticleDO> searchByKeyword(String keyword, HttpServletRequest request) {
        int curr = parseCurr(request);
        int limit = parseLimit(request);
        String safeKeyword = escapeLike(keyword);

        int start = (curr - 1) * limit;
        List<ArticleDO> list = articleInfoMapper.searchByKeyword(safeKeyword, start, limit);
        int total = articleInfoMapper.countByKeyword(safeKeyword);
        int pageSize = (int) Math.ceil(total / (double) limit);

        Page<ArticleDO> page = new Page<>();
        page.setLists(list)
                .setCurr(curr)
                .setSize(limit)
                .setPageSize(pageSize)
                .setPageLine(PageUtil.pcnDefault(curr, pageSize));
        return page;
    }

    /** 按 sn 列表批量取文章（按 sn 顺序）。 */
    public List<ArticleDO> listBySnList(List<Integer> sns) {
        if (sns == null || sns.isEmpty()) {
            return new ArrayList<>(0);
        }
        return articleInfoMapper.selectListBySnList(sns);
    }

    /** 取热门文章 TOP-N。 */
    public List<ArticleDO> listHotTopN(int n) {
        if (n <= 0) {
            return new ArrayList<>(0);
        }
        return articleInfoMapper.selectHotTopN(n);
    }

    /** 删除文章并联动评论。 */
    public String removeWithReview(String sn) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
            // 入参非数字直接返回失败，不抛异常（外层是渐进迁移期间的字符串协议）
            return OPS_FAIL;
        }

        if (articleInfoMapper.deleteById(id) <= 0) {
            return OPS_FAIL;
        }
        if (log.isDebugEnabled()) {
            log.debug("delete article: {}", id);
        }
        return OPS_SUCCESS;
    }

    /** 更新指定文章的封面图 URL。 */
    public int updatePictureUrlById(ArticleDO articleInfo) {
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
     *
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
