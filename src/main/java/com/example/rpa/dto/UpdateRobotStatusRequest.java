package com.example.rpa.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改机器人状态请求
 */
@Data
public class UpdateRobotStatusRequest {

    @NotNull(message = "机器人状态不能为空")
    private Integer status;
}
