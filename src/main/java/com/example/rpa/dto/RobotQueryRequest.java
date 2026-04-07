package com.example.rpa.dto;

import lombok.Data;

/**
 * 机器人分页查询请求
 */
@Data
public class RobotQueryRequest {

    private Integer current = 1;

    private Integer size = 10;

    private String keyword;

    private Integer status;
}
