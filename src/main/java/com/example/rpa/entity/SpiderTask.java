package com.example.rpa.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 爬虫任务实体类
 */
@Data
@TableName("spider_task")
public class SpiderTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 任务类型：1-定时任务，2-手动任务
     */
    private Integer taskType;

    /**
     * 目标 URL
     */
    private String url;

    /**
     * 爬虫脚本内容 (Groovy)
     */
    private String scriptContent;

    /**
     * Cron 表达式
     */
    private String cronExpression;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;

    /**
     * 上次运行时间
     */
    private LocalDateTime lastRunTime;

    /**
     * 下次运行时间
     */
    private LocalDateTime nextRunTime;

    /**
     * 运行次数
     */
    private Integer runCount;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
