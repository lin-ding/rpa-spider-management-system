package com.example.rpa.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑任务请求
 */
@Data
public class UpdateTaskRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotNull(message = "关联流程不能为空")
    private Long processId;

    @NotNull(message = "关联机器人不能为空")
    private Long robotId;

    private String taxpayerId;

    private String enterpriseName;

    private String category;

    @Min(value = 1, message = "优先级最小为 1")
    @Max(value = 3, message = "优先级最大为 3")
    private Integer priority;

    @Min(value = 0, message = "配置状态非法")
    @Max(value = 1, message = "配置状态非法")
    private Integer configStatus;

    private String remark;
}
