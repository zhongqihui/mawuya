package com.zqh.blog.mapper;

import com.zqh.blog.entity.ArticleInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    public void test() {
        System.out.println("hello");
    }
}