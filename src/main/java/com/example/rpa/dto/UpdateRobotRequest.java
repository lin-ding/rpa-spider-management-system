package com.example.rpa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.io.Serializable;

@Data
public class UpdateRobotRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "机器人名称不能为空")
    private String robotName;

    private String type;

    private String ip;

    private Integer port;

    private String description;

    private String remark;
}
