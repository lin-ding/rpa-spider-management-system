package com.example.rpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rpa.entity.TaskExecutionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务执行日志 Mapper 接口
 */
@Mapper
public interface TaskExecutionLogMapper extends BaseMapper<TaskExecutionLog> {
}
