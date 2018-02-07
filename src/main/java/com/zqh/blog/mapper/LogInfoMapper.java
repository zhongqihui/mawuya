package com.zqh.blog.mapper;


import com.zqh.blog.entity.LogInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/7 16:50
 * description: 访客记录mapper
 **/
@Repository
public interface LogInfoMapper extends BaseMapper<LogInfo,Integer> {
    int insertBatch(List<LogInfo> list);
}
