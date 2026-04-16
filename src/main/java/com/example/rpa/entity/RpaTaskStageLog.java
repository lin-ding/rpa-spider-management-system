package com.example.rpa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务阶段日志
 */
@Data
@TableName("rpa_task_stage_log")
public class RpaTaskStageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long executionId;

    private Long taskId;

    private Integer stageOrder;

    private String stageName;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String errorMessage;

    private String logDetail;

    private String stageResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
