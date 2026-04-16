package com.example.rpa.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务执行记录列表项
 */
@Data
public class TaskExecutionListItemVO {

    private Long id;

    private Long taskId;

    private String executionNo;

    private String taskName;

    private String processName;

    private String robotName;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String errorMessage;

    private String triggerType;

    private LocalDateTime createTime;
}
