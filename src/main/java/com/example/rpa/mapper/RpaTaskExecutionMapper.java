package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.RpaTaskExecution;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务执行记录 Mapper
 */
@Mapper
public interface RpaTaskExecutionMapper extends BaseMapper<RpaTaskExecution> {
}
