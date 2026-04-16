package com.example.rpa.vo;

import lombok.Data;

/**
 * 任务统计汇总
 */
@Data
public class TaskStatisticsVO {

    private long total;

    private long pending;

    private long queued;

    private long running;

    private long completed;

    private long failed;
}
