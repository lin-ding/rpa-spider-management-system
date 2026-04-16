package com.example.rpa.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务执行记录
 */
@Data
@TableName("rpa_task_execution")
public class RpaTaskExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String executionNo;

    private Long taskId;

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

    private String triggerType;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private String errorMessage;

    private String logDetail;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
