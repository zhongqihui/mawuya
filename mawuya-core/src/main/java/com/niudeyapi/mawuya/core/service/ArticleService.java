/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.service;

import com.niudeyapi.mawuya.common.utils.DateUtil;
import com.niudeyapi.mawuya.common.utils.PageUtil;
import com.niudeyapi.mawuya.core.dataobject.ArticleDO;
import com.niudeyapi.mawuya.core.dataobject.ArticleStatusLogDO;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.mapper.ArticleInfoMapper;
import com.niudeyapi.mawuya.core.mapper.ArticleStatusLogMapper;
import com.niudeyapi.mawuya.core.vo.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * <h3>文章状态机</h3>
 * <pre>
 *   [新建草稿] ──save(DRAFT)──▶ DRAFT(0) ──publish──▶ PUBLISHED(1)
 *                                  ▲                      │
 *                                  │                  withdraw
 *                                  │                      ▼
 *                              (update)            WITHDRAWN(2)
 *                                  │                      │
 *                                  └──── republish ◀──────┘
 * </pre>
 *
 * <p>所有 AMS（对外）只读 API 都通过本类的 {@code listPublishedXxx} / {@code getPublishedById}
 * 等方法访问，保证「草稿与已撤回」对外不可见。</p>
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

    // ===================== 文章状态常量（与 article_info.status 一致） =====================
    /** 状态：草稿 */
    public static final int STATUS_DRAFT     = 0;
    /** 状态：已发布（AMS 对外可见） */
    public static final int STATUS_PUBLISHED = 1;
    /** 状态：已撤回（可二次编辑后重新发布，对外不可见） */
    public static final int STATUS_WITHDRAWN = 2;

    // ===================== 状态流转日志 action 常量 =====================
    private static final String ACTION_CREATE_DRAFT      = "CREATE_DRAFT";
    private static final String ACTION_CREATE_PUBLISHED  = "CREATE_PUBLISHED";
    private static final String ACTION_UPDATE_DRAFT      = "UPDATE_DRAFT";
    private static final String ACTION_UPDATE_PUBLISHED  = "UPDATE_PUBLISHED";
    private static final String ACTION_PUBLISH           = "PUBLISH";
    private static final String ACTION_WITHDRAW          = "WITHDRAW";
    private static final String ACTION_REPUBLISH         = "REPUBLISH";

    private final ArticleInfoMapper articleInfoMapper;
    private final ArticleStatusLogMapper statusLogMapper;

    @Autowired
    public ArticleService(ArticleInfoMapper baseMapper, ArticleStatusLogMapper statusLogMapper) {
        super(baseMapper);
        this.articleInfoMapper = baseMapper;
        this.statusLogMapper = statusLogMapper;
    }

    // ============================== AMS 对外查询（仅已发布） ==============================

    /** 取下一篇文章（已限定 status=1）。 */
    public ArticleDO getNextById(Integer aid) {
        return articleInfoMapper.selectNextById(aid);
    }

    /** 取上一篇文章（已限定 status=1）。 */
    public ArticleDO getPrevById(Integer aid) {
        return articleInfoMapper.selectPrevById(aid);
    }

    /** 详情页：仅返回已发布的文章，否则返回 null（让上层走 404）。 */
    public ArticleDO getPublishedById(Integer aid) {
        ArticleDO a = articleInfoMapper.selectById(aid);
        if (a == null) {
            return null;
        }
        Integer st = a.getStatus();
        return st != null && st == STATUS_PUBLISHED ? a : null;
    }

    /** AMS 首页：从 request 解析分页参数，仅查 status=1 的文章。 */
    public Page<ArticleDO> listPublishedByPageFromRequest(HttpServletRequest request) {
        return listByPageWithStatus(parseCurr(request), parseLimit(request), STATUS_PUBLISHED);
    }

    /** AMS 归档页：按年份分组，仅含已发布。 */
    public Map<String, List<ArticleDO>> listPublishedGroupByYear() {
        Map<String, Object> map = new HashMap<>(EMPTY_CONDITION_CAPACITY);
        map.put("status", STATUS_PUBLISHED);
        List<ArticleDO> infos = articleInfoMapper.selectAllNoContent(map);
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

    /** AMS 已发布文章总数（首页统计、归档计数等）。 */
    public int countPublished() {
        Map<String, Object> cond = new HashMap<>(EMPTY_CONDITION_CAPACITY);
        cond.put("status", STATUS_PUBLISHED);
        return articleInfoMapper.selectCount(cond);
    }

    /** AMS sitemap / RSS 等只读全量：仅取已发布。 */
    public List<ArticleDO> listPublishedAllNoContent() {
        Map<String, Object> cond = new HashMap<>(EMPTY_CONDITION_CAPACITY);
        cond.put("status", STATUS_PUBLISHED);
        return articleInfoMapper.selectAllNoContent(cond);
    }

    /**
     * AMS 关键字搜索（仅已发布）。
     *
     * <p>关键字会做 SQL LIKE 通配符转义，{@code %} 被去除避免大范围扫描；
     * 同时 keyword 通过 {@code #{}} 参数化绑定，无 SQL 注入风险。</p>
     */
    public Page<ArticleDO> searchPublishedByKeyword(String keyword, HttpServletRequest request) {
        int curr = parseCurr(request);
        int limit = parseLimit(request);
        String safeKeyword = escapeLike(keyword);

        int start = (curr - 1) * limit;
        List<ArticleDO> list = articleInfoMapper.searchByKeyword(safeKeyword, STATUS_PUBLISHED, start, limit);
        int total = articleInfoMapper.countByKeyword(safeKeyword, STATUS_PUBLISHED);
        int pageSize = (int) Math.ceil(total / (double) limit);

        Page<ArticleDO> page = new Page<>();
        page.setLists(list)
                .setCurr(curr)
                .setSize(limit)
                .setPageSize(pageSize)
                .setPageLine(PageUtil.pcnDefault(curr, pageSize));
        return page;
    }

    /** AMS 按 sn 列表批量取（标签页用），仅已发布。 */
    public List<ArticleDO> listPublishedBySnList(List<Integer> sns) {
        if (sns == null || sns.isEmpty()) {
            return new ArrayList<>(0);
        }
        return articleInfoMapper.selectListBySnList(sns, STATUS_PUBLISHED);
    }

    /** AMS 热门文章 TOP-N（仅已发布）。 */
    public List<ArticleDO> listPublishedHotTopN(int n) {
        if (n <= 0) {
            return new ArrayList<>(0);
        }
        return articleInfoMapper.selectHotTopN(n, STATUS_PUBLISHED);
    }

    // ============================== BMS 后台查询（不限制状态） ==============================

    /**
     * BMS 列表：可按 status 过滤；当 {@code status} 为 null 时返回全部文章。
     */
    public List<ArticleDO> listAllNoContentByStatus(Integer status) {
        Map<String, Object> cond = new HashMap<>(EMPTY_CONDITION_CAPACITY);
        if (status != null) {
            cond.put("status", status);
        }
        return articleInfoMapper.selectAllNoContent(cond);
    }

    /** BMS 列表（不过滤状态），保留与历史调用方兼容的入口。 */
    public List<ArticleDO> listAllNoContent(Map<String, ?> map) {
        return articleInfoMapper.selectAllNoContent(map);
    }

    // ============================== 写操作（含状态流转） ==============================

    /**
     * 新增草稿：直接以 status=DRAFT 落库，并写一条状态日志。
     *
     * @return 主键 sn（>0 表示成功）
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveDraft(ArticleDO a, String operator) {
        normalizeForInsert(a, STATUS_DRAFT);
        int rows = articleInfoMapper.insert(a);
        if (rows > 0 && a.getSn() != null) {
            writeStatusLog(a.getSn(), null, STATUS_DRAFT, ACTION_CREATE_DRAFT, operator, null);
        }
        return rows;
    }

    /**
     * 新增并发布：直接以 status=PUBLISHED 落库，写一条状态日志。
     */
    @Transactional(rollbackFor = Exception.class)
    public int savePublished(ArticleDO a, String operator) {
        normalizeForInsert(a, STATUS_PUBLISHED);
        int rows = articleInfoMapper.insert(a);
        if (rows > 0 && a.getSn() != null) {
            writeStatusLog(a.getSn(), null, STATUS_PUBLISHED, ACTION_CREATE_PUBLISHED, operator, null);
        }
        return rows;
    }

    /**
     * 草稿场景下「保存草稿」更新：保持 status=DRAFT，不改动其它流转。
     * <p>不允许已发布 / 已撤回的文章退回草稿（避免业务语义混乱）。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateAsDraft(ArticleDO a, String operator) {
        ArticleDO db = mustGet(a.getSn());
        if (db.getStatus() != null && db.getStatus() != STATUS_DRAFT) {
            throw new BusinessException("只有草稿可以「保存为草稿」");
        }
        // 保持业务字段更新，但 status 仍是 DRAFT
        carryOverCounters(a, db);
        a.setStatus(STATUS_DRAFT);
        a.setInsertTime(db.getInsertTime());
        int rows = articleInfoMapper.update(a);
        if (rows > 0) {
            writeStatusLog(a.getSn(), STATUS_DRAFT, STATUS_DRAFT, ACTION_UPDATE_DRAFT, operator, null);
        }
        return rows;
    }

    /**
     * 已发布 / 已撤回 文章的常规更新：保持原状态不变。
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateKeepStatus(ArticleDO a, String operator) {
        ArticleDO db = mustGet(a.getSn());
        Integer st = db.getStatus();
        carryOverCounters(a, db);
        a.setStatus(st);
        a.setInsertTime(db.getInsertTime());
        int rows = articleInfoMapper.update(a);
        if (rows > 0) {
            String action = st != null && st == STATUS_WITHDRAWN ? ACTION_UPDATE_DRAFT : ACTION_UPDATE_PUBLISHED;
            writeStatusLog(a.getSn(), st, st, action, operator, null);
        }
        return rows;
    }

    /**
     * 草稿 → 发布：状态切到 PUBLISHED，写流转日志；如果 insert_time 为 NULL（首次发布），
     * 由 SQL 端用 COALESCE 补上 NOW()。
     */
    @Transactional(rollbackFor = Exception.class)
    public int publish(Integer sn, String operator) {
        ArticleDO db = mustGet(sn);
        Integer st = db.getStatus();
        if (st != null && st == STATUS_PUBLISHED) {
            return 0;
        }
        if (st != null && st != STATUS_DRAFT && st != STATUS_WITHDRAWN) {
            throw new BusinessException("当前状态不支持发布操作");
        }
        int rows = articleInfoMapper.updateStatus(sn, STATUS_PUBLISHED, true);
        if (rows > 0) {
            writeStatusLog(sn, st, STATUS_PUBLISHED,
                    st != null && st == STATUS_WITHDRAWN ? ACTION_REPUBLISH : ACTION_PUBLISH,
                    operator, null);
        }
        return rows;
    }

    /**
     * 已发布 → 已撤回：切换状态，文章对外不可见，可在 BMS 列表「已撤回」tab 中找到并编辑。
     */
    @Transactional(rollbackFor = Exception.class)
    public int withdraw(Integer sn, String operator, String remark) {
        ArticleDO db = mustGet(sn);
        Integer st = db.getStatus();
        if (st == null || st != STATUS_PUBLISHED) {
            throw new BusinessException("只有已发布的文章才能撤回");
        }
        int rows = articleInfoMapper.updateStatus(sn, STATUS_WITHDRAWN, false);
        if (rows > 0) {
            writeStatusLog(sn, STATUS_PUBLISHED, STATUS_WITHDRAWN, ACTION_WITHDRAW, operator, remark);
        }
        return rows;
    }

    /**
     * 已撤回 → 已发布：仅切状态，不重置 insert_time（首发时间保留），update_time 由 SQL 端置 NOW()。
     */
    @Transactional(rollbackFor = Exception.class)
    public int republish(Integer sn, String operator) {
        ArticleDO db = mustGet(sn);
        Integer st = db.getStatus();
        if (st == null || st != STATUS_WITHDRAWN) {
            throw new BusinessException("只有已撤回的文章才能重新发布");
        }
        int rows = articleInfoMapper.updateStatus(sn, STATUS_PUBLISHED, true);
        if (rows > 0) {
            writeStatusLog(sn, STATUS_WITHDRAWN, STATUS_PUBLISHED, ACTION_REPUBLISH, operator, null);
        }
        return rows;
    }

    /** 列出某文章的状态流转日志（详情页时间线）。 */
    public List<ArticleStatusLogDO> listStatusLogs(Integer articleSn) {
        return statusLogMapper.selectByArticleSn(articleSn);
    }

    /** 删除文章并联动评论（注意：硬删除会同步删除状态日志的关联意义） */
    public String removeWithReview(String sn) {
        int id;
        try {
            id = Integer.parseInt(sn);
        } catch (NumberFormatException e) {
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

    /** 更新指定文章的封面图 URL（不影响 status / 不写流转日志）。 */
    public int updatePictureUrlById(ArticleDO articleInfo) {
        return articleInfoMapper.updatePictureUrl(articleInfo);
    }

    // ============================== 内部工具 ==============================

    /** 强制获取文章，找不到抛业务异常。 */
    private ArticleDO mustGet(Integer sn) {
        if (sn == null) {
            throw new BusinessException("文章 sn 不能为空");
        }
        ArticleDO db = articleInfoMapper.selectById(sn);
        if (db == null) {
            throw new BusinessException("文章不存在");
        }
        return db;
    }

    /** 把 DB 中的计数字段（read/review/praise/tease）保留到新对象上，避免被前端 form 提交清零。 */
    private void carryOverCounters(ArticleDO target, ArticleDO db) {
        if (target.getReadNum() == null)   target.setReadNum(db.getReadNum());
        if (target.getReviewNum() == null) target.setReviewNum(db.getReviewNum());
        if (target.getPraiseNum() == null) target.setPraiseNum(db.getPraiseNum());
        if (target.getTeaseNum() == null)  target.setTeaseNum(db.getTeaseNum());
        if (target.getPictureUrl() == null) target.setPictureUrl(db.getPictureUrl());
    }

    /** 新增时把计数字段补 0，避免 NPE；并设置初始 status。 */
    private void normalizeForInsert(ArticleDO a, int status) {
        if (a.getReadNum() == null)   a.setReadNum(0);
        if (a.getReviewNum() == null) a.setReviewNum(0);
        if (a.getPraiseNum() == null) a.setPraiseNum(0);
        if (a.getTeaseNum() == null)  a.setTeaseNum(0);
        if (a.getCategorySn() == null) a.setCategorySn(0);
        a.setStatus(status);
    }

    /** 写一条流转日志；任何写流转的入口都通过此方法，便于后续接审计 / 监听。 */
    private void writeStatusLog(Integer articleSn, Integer fromStatus, Integer toStatus,
                                 String action, String operator, String remark) {
        try {
            ArticleStatusLogDO row = new ArticleStatusLogDO()
                    .setArticleSn(articleSn)
                    .setFromStatus(fromStatus)
                    .setToStatus(toStatus)
                    .setAction(action)
                    .setOperator(operator)
                    .setRemark(remark);
            statusLogMapper.insert(row);
        } catch (Exception e) {
            // 流转日志失败不阻塞主流程，但要把上下文打出来便于排查
            log.warn("[article-status-log] insert failed: sn={}, action={}, err={}",
                    articleSn, action, e.getMessage());
        }
    }

    /** 分页查询，支持按 status 过滤。 */
    private Page<ArticleDO> listByPageWithStatus(int curr, int limit, Integer status) {
        if (curr < 1) curr = 1;
        if (limit < 1) limit = DEFAULT_PAGE_SIZE;

        Map<String, Object> cond = new HashMap<>(EMPTY_CONDITION_CAPACITY);
        cond.put("start", (curr - 1) * limit);
        cond.put("limit", limit);
        if (status != null) {
            cond.put("status", status);
        }
        List<ArticleDO> list = articleInfoMapper.selectByPage(cond);

        Map<String, Object> countCond = new HashMap<>(EMPTY_CONDITION_CAPACITY);
        if (status != null) {
            countCond.put("status", status);
        }
        int total = articleInfoMapper.selectCount(countCond);
        int pageSize = (int) Math.ceil(total / (double) limit);

        Page<ArticleDO> page = new Page<>();
        page.setLists(list)
                .setCurr(curr)
                .setSize(limit)
                .setPageSize(pageSize)
                .setPageLine(PageUtil.pcnDefault(curr, pageSize));
        return page;
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
