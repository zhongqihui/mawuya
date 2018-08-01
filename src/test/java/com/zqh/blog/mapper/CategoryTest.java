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
        String a = "好\uD83D\uDE09我";
        Category c = new Category().setCategoryName(a);
        int i = categoryMapper.insert(c);
        System.out.println(i);
    }
}
