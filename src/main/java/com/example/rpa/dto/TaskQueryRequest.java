package com.example.rpa.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 任务分页查询请求
 */
@Data
public class TaskQueryRequest {

    private Integer current = 1;

    private Integer size = 10;

    private String taskCode;

    private String taskName;

    private String executionStatus;

    private Long processId;

    private Long robotId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeTo;
}
