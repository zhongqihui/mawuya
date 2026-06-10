/*
 * www.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.niudeyapi.mawuya.core.service;

import com.niudeyapi.mawuya.core.dataobject.TagDO;
import com.niudeyapi.mawuya.core.exception.BusinessException;
import com.niudeyapi.mawuya.core.mapper.TagMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 标签业务层。
 *
 * <p>遵循阿里 Service 命名规约：方法前缀 {@code get/list/save/update/remove}。</p>
 *
 * <p>所有写操作都通过本 service 走 {@link Transactional} 保证一致性：
 * <ul>
 *   <li>新增标签：tagName 唯一约束（由 DB {@code uk_tag_name} 兜底，service 提前查询给友好错误）；
 *       tagColor 缺省给出系统默认调色板；</li>
 *   <li>删除标签：先清掉 {@code article_tag} 关联再删 {@code tag_info}；</li>
 *   <li>重绑文章标签：{@link #rebindArticleTags} 在事务内"先解绑全部、再批量绑定"。</li>
 * </ul></p>
 *
 * @author 钟启辉
 */
@Service
public class TagService extends BaseService<TagDO, Integer> {

    /** 标签名最大长度（与 DB 列 VARCHAR(30) 对齐） */
    public static final int MAX_TAG_NAME_LENGTH = 30;
    /** 标签颜色最大长度（与 DB 列 VARCHAR(20) 对齐） */
    public static final int MAX_TAG_COLOR_LENGTH = 20;
    /** 标签默认配色（与博客主蓝色调一致，BMS 创建未填色时使用） */
    private static final String DEFAULT_TAG_COLOR = "#2494f2";
    /** 合法的 HEX 颜色正则：#rgb / #rrggbb（不含 alpha 通道，避免 CSS 注入） */
    private static final java.util.regex.Pattern HEX_COLOR = java.util.regex.Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");

    private final TagMapper tagMapper;

    @Autowired
    public TagService(TagMapper baseMapper) {
        super(baseMapper);
        this.tagMapper = baseMapper;
    }

    // ============================ 查询 ============================

    /**
     * 列出标签云（带文章计数，含全部状态文章）。
     * <p>BMS 标签管理页/编辑页下拉等管理场景使用，便于知晓「真实关联数」。
     * AMS 前台请用 {@link #listPublishedTagCloud()}。</p>
     */
    public List<TagDO> listTagCloud() {
        return tagMapper.selectAllWithArtSize();
    }

    /**
     * AMS 前台标签云：artSize 仅统计 status=1 的已发布文章。
     * <p>草稿/已撤回的文章不计入对外的标签热度，与右侧栏其他模块（最新评论、
     * 分类计数、热门文章）保持 published-only 一致。</p>
     */
    public List<TagDO> listPublishedTagCloud() {
        return tagMapper.selectAllWithPublishedArtSize();
    }

    /** 列出某文章关联的所有标签。 */
    public List<TagDO> listByArticleSn(Integer articleSn) {
        if (articleSn == null) {
            return Collections.emptyList();
        }
        return tagMapper.selectByArticleSn(articleSn);
    }

    /** 列出某标签下的文章 sn 列表（全部状态，BMS 管理用）。 */
    public List<Integer> listArticleSnByTag(Integer tagSn) {
        if (tagSn == null) {
            return Collections.emptyList();
        }
        return tagMapper.selectArticleSnByTag(tagSn);
    }

    /**
     * 列出某标签下「已发布」的文章 sn 列表（AMS /tags/{sn} 详情页用）。
     * <p>避免点击标签后看到 404（已撤回文章会被 ArticleService.getPublishedArticle 拦截）。</p>
     */
    public List<Integer> listPublishedArticleSnByTag(Integer tagSn) {
        if (tagSn == null) {
            return Collections.emptyList();
        }
        return tagMapper.selectPublishedArticleSnByTag(tagSn);
    }

    /** 列出某文章关联标签的 sn 列表（仅 id，用于 BMS 编辑回填）。 */
    public List<Integer> listTagSnByArticle(Integer articleSn) {
        if (articleSn == null) {
            return Collections.emptyList();
        }
        List<TagDO> tags = tagMapper.selectByArticleSn(articleSn);
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> sns = new ArrayList<>(tags.size());
        for (TagDO t : tags) {
            sns.add(t.getSn());
        }
        return sns;
    }

    // ============================ 写操作 ============================

    /**
     * 新建标签。
     *
     * <p>tagName 必填且不能与已有标签重名；tagColor 不传或非法时使用默认色。</p>
     *
     * @return 主键 sn（>0 表示成功）
     */
    @Transactional(rollbackFor = Exception.class)
    public int save(String tagName, String tagColor) {
        String name = normalizeName(tagName);
        if (existsByName(name)) {
            throw new BusinessException("标签「" + name + "」已存在");
        }
        TagDO t = new TagDO()
                .setTagName(name)
                .setTagColor(normalizeColor(tagColor));
        return tagMapper.insert(t);
    }

    /**
     * 「按名称获取或创建」标签：
     * BMS 端「为文章打标签」UI 允许用户直接输入新标签名，提交时若不存在则自动创建。
     * 与 {@link #save} 区别：已存在时返回现有标签，不抛异常。
     */
    @Transactional(rollbackFor = Exception.class)
    public TagDO getOrCreateByName(String tagName) {
        String name = normalizeName(tagName);
        TagDO existing = findByName(name);
        if (existing != null) {
            return existing;
        }
        TagDO t = new TagDO().setTagName(name).setTagColor(DEFAULT_TAG_COLOR);
        if (tagMapper.insert(t) <= 0 || t.getSn() == null) {
            // 高并发下可能被另一线程抢先插入，触发唯一键冲突；再查一次兜底
            existing = findByName(name);
            if (existing != null) {
                return existing;
            }
            throw new BusinessException("创建标签失败：" + name);
        }
        return t;
    }

    /** 按 sn 更新标签：名称变更需做唯一性校验。 */
    @Transactional(rollbackFor = Exception.class)
    public int updateByIdSafely(Integer sn, String tagName, String tagColor) {
        if (sn == null) {
            throw new BusinessException("sn 不能为空");
        }
        TagDO db = tagMapper.selectById(sn);
        if (db == null) {
            throw new BusinessException("标签不存在");
        }
        String name = normalizeName(tagName);
        if (!name.equals(db.getTagName())) {
            TagDO other = findByName(name);
            if (other != null && !other.getSn().equals(sn)) {
                throw new BusinessException("标签「" + name + "」已存在");
            }
        }
        TagDO patch = new TagDO()
                .setSn(sn)
                .setTagName(name)
                .setTagColor(normalizeColor(tagColor));
        return tagMapper.update(patch);
    }

    /**
     * 删除标签：先清空 {@code article_tag} 中所有引用本标签的关联，再删 {@code tag_info}。
     * <p>这样可以保证 AMS 侧边栏的标签云、文章详情页的标签徽章实时一致。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public int removeWithRelations(Integer sn) {
        if (sn == null) {
            throw new BusinessException("sn 不能为空");
        }
        if (tagMapper.selectById(sn) == null) {
            throw new BusinessException("标签不存在");
        }
        // 1) 解掉所有文章绑定（不会因不存在关联而失败）
        tagMapper.unbindByTagSn(sn);
        // 2) 删 tag_info
        return tagMapper.deleteById(sn);
    }

    /**
     * 重新绑定文章与标签：先解绑全部，再绑定新的列表。
     *
     * @param articleSn 文章 sn
     * @param tagSns    新的标签 sn 列表（可为 null/empty → 清空所有标签）
     * @return 实际新绑定的行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int rebindArticleTags(Integer articleSn, List<Integer> tagSns) {
        if (articleSn == null) {
            return 0;
        }
        tagMapper.unbindByArticle(articleSn);
        if (tagSns == null || tagSns.isEmpty()) {
            return 0;
        }
        // 去重，避免重复绑定（INSERT IGNORE 也能挡住，这里再去重一次节省 SQL）
        Set<Integer> deduped = new LinkedHashSet<>(tagSns);
        int n = 0;
        for (Integer tagSn : deduped) {
            if (tagSn != null) {
                n += tagMapper.bindArticleTag(articleSn, tagSn);
            }
        }
        return n;
    }

    /**
     * 重新绑定文章与标签——支持混合传入「已有 sn」与「新标签名」：
     * 名称会自动转化为新建/已有标签的 sn 后再走 {@link #rebindArticleTags}。
     */
    @Transactional(rollbackFor = Exception.class)
    public int rebindArticleTagsByMixed(Integer articleSn,
                                        List<Integer> existingTagSns,
                                        List<String> newTagNames) {
        if (articleSn == null) {
            return 0;
        }
        List<Integer> finalSns = new ArrayList<>(
                (existingTagSns == null ? 0 : existingTagSns.size())
                + (newTagNames == null ? 0 : newTagNames.size()));
        if (existingTagSns != null) {
            for (Integer s : existingTagSns) {
                if (s != null) finalSns.add(s);
            }
        }
        if (newTagNames != null) {
            for (String n : newTagNames) {
                if (StringUtils.isBlank(n)) continue;
                TagDO t = getOrCreateByName(n);
                if (t != null && t.getSn() != null) {
                    finalSns.add(t.getSn());
                }
            }
        }
        return rebindArticleTags(articleSn, finalSns);
    }

    // ============================ 内部 ============================

    private String normalizeName(String raw) {
        if (StringUtils.isBlank(raw)) {
            throw new BusinessException("标签名称不能为空");
        }
        String trimmed = raw.trim();
        if (trimmed.length() > MAX_TAG_NAME_LENGTH) {
            throw new BusinessException("标签名称最长 " + MAX_TAG_NAME_LENGTH + " 字符");
        }
        return trimmed;
    }

    private String normalizeColor(String raw) {
        if (StringUtils.isBlank(raw)) {
            return DEFAULT_TAG_COLOR;
        }
        String c = raw.trim();
        if (c.length() > MAX_TAG_COLOR_LENGTH) {
            throw new BusinessException("标签颜色字符串过长");
        }
        // 白名单：仅接受 HEX；非法格式视为攻击/拼写错误，回退默认色
        if (!HEX_COLOR.matcher(c).matches()) {
            return DEFAULT_TAG_COLOR;
        }
        return c;
    }

    private boolean existsByName(String name) {
        return findByName(name) != null;
    }

    private TagDO findByName(String name) {
        Map<String, Object> cond = new HashMap<>(2);
        cond.put("tagName", name);
        List<TagDO> list = tagMapper.selectByName(cond);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }
}
