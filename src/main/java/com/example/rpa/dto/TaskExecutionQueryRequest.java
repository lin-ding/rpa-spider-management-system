package com.example.rpa.dto;

import lombok.Data;

/**
 * 任务执行记录分页查询请求
 */
@Data
public class TaskExecutionQueryRequest {

    private Integer current = 1;

    private Integer size = 10;
}
