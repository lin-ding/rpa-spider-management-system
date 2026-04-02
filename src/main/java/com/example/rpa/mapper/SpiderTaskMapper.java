package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.SpiderTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 爬虫任务 Mapper 接口
 */
@Mapper
public interface SpiderTaskMapper extends BaseMapper<SpiderTask> {
}
