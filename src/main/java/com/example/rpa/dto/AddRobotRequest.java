package com.example.rpa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增机器人请求
 */
@Data
public class AddRobotRequest {

    @NotBlank(message = "机器人名称不能为空")
    private String robotName;

    @NotBlank(message = "机器人编码不能为空")
    private String robotCode;

    @NotNull(message = "机器人类型不能为空")
    private Integer robotType;

    private String hostName;

    private String hostIp;

    @Min(value = 1, message = "端口必须大于 0")
    @Max(value = 65535, message = "端口不能超过 65535")
    private Integer port;

    private String clientVersion;

    private String description;
}
