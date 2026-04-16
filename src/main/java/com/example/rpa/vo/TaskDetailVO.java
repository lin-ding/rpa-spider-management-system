package com.example.rpa.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务详情
 */
@Data
public class TaskDetailVO {

    private Long id;

    private String taskCode;

    private String taskName;

    private Long processId;

    private String processName;

    private Long robotId;

    private String robotName;

    private String taxpayerId;

    private String enterpriseName;

    private String category;

    private Integer priority;

    private Integer configStatus;

    private String executionStatus;

    private String currentStatus;

    private String latestExecutionStatus;

    private LocalDateTime lastStartTime;

    private LocalDateTime lastEndTime;

    private String remark;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
