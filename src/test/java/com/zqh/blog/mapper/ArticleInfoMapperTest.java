package com.zqh.blog.mapper;

import com.zqh.blog.controller.IndexController;
import com.zqh.blog.entity.ArticleInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
* @author 钟启辉
* @company www.jiweitech.com
* @date 2018/1/18 11:42
* @description
**/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-core.xml"})
public class ArticleInfoMapperTest {
    @Autowired
    ArticleInfoMapper articleInfoMapper;
    @Autowired
    IndexController indexController;

    @Test
    public void getArticle() {
        ArticleInfo articleInfo = articleInfoMapper.selectById(1);
        System.out.println(articleInfo);
    }


    @Test
    public void testSelectCount() {
        Map<String, String> map = new HashMap<>();
        int count = articleInfoMapper.selectCount(map);
        System.out.println(count);
    }

    @Test
    public void testInsert() {
        ArticleInfo articleInfo = new ArticleInfo()
                .setArticleTitle("java")
                .setArticleSummary("我是概要")
                .setArticleContent("我是内容");
        int insert = articleInfoMapper.insert(articleInfo);
        System.out.println(insert);
    }

    @Test
    public void testDel() {
        int i = articleInfoMapper.deleteById(1);
        System.out.println(i);
    }

    @Test
    public void testUpdate() {
        ArticleInfo info = articleInfoMapper.selectById(2);
        info.setArticleContent("我是内容2").setArticleSummary("我是概要2");
        int update = articleInfoMapper.update(info);
        System.out.println(update);
    }

    @Test
    public void testAop() {

    }
}