package com.example.rpa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 机器人心跳上报请求
 */
@Data
public class RobotHeartbeatRequest {

    @NotBlank(message = "机器人编码不能为空")
    private String robotCode;
}
