package com.zqh.blog.mapper;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 封装mapper增删改查
 * Created by zhongqihui on 2017/2/10.
 */
public interface BaseMapper<T, PK extends Serializable> {

    /**
     * 插入操作
     *
     * @param t 插入的对象
     */
    void insert(T t);

    /**
     * 删除操作
     *
     * @param id 根据主键来删除
     */
    void deleteById(PK id);

    /**
     * 修改操作
     *
     * @param t
     */
    void update(T t);

    /**
     * 根据PK查找对象
     *
     * @param pk
     * @return
     */
    T selectById(PK pk);

    /**
     * 模糊查询，根据多条件查询List
     *
     * @param map
     * @return
     */
    List<T> selectList(Map map);

    /**
     * 分页查询
     * map中封装了查询条件，和分页的开始和结束等，key为start,limit,entity属性
     * @return List对象
     */
    List<T> selectByPage(Map map);

    /**
     * 根据map条件查询记录数
     * @param map
     * @return
     */
    int selectCount(Map map);

}
