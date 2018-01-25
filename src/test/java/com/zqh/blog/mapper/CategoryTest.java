package com.zqh.blog.mapper;

import com.zqh.blog.entity.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-core.xml"})
public class CategoryTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    public void testInsert() {
//        Category c = Category.of("java语言");
        Category c = new Category().setCName("生活情感");
        int i = categoryMapper.insert(c);
        System.out.println(i);
    }
}
