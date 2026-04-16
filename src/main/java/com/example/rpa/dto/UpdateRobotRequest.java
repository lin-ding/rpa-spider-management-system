package com.example.rpa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改机器人请求
 */
@Data
public class UpdateRobotRequest {

    @NotNull(message = "机器人ID不能为空")
    private Long id;

    @NotBlank(message = "机器人名称不能为空")
    private String robotName;

    @NotNull(message = "机器人类型不能为空")
    private Integer robotType;

    private String hostName;

    private String hostIp;

    @Min(value = 1, message = "端口必须大于 0")
    @Max(value = 65535, message = "端口不能超过 65535")
    private Integer port;

    private String clientVersion;

    private String description;

    private Integer status;
}
