package com.example.rpa.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务阶段日志视图
 */
@Data
public class TaskStageLogVO {

    private Long id;

    private Long executionId;

    private Integer stageOrder;

    private String stageName;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String errorMessage;

    private String logDetail;

    private String stageResult;

    private LocalDateTime createTime;
}
