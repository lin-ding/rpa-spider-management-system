package com.example.rpa.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务列表项
 */
@Data
public class TaskListItemVO {

    private Long id;

    private String taskCode;

    private String taskName;

    private Long processId;

    private String processName;

    private Long robotId;

    private String robotName;

    private String executionStatus;

    private String currentStatus;

    private String latestExecutionStatus;

    private LocalDateTime lastStartTime;

    private LocalDateTime lastEndTime;

    private LocalDateTime createTime;
}
