package com.example.rpa.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 执行监控分页查询请求
 */
@Data
public class ExecutionQueryRequest {

    private Integer current = 1;

    private Integer size = 15;

    private String taskName;

    private String robotName;

    private String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeTo;
}
