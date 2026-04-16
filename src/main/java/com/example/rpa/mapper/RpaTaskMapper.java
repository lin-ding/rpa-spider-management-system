package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.RpaTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务 Mapper
 */
@Mapper
public interface RpaTaskMapper extends BaseMapper<RpaTask> {
}
