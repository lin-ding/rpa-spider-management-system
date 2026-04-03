package com.example.rpa.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessDetailVO {

    private Long id;

    private String processName;

    private String processCode;

    private String description;

    private Integer processType;

    private String scriptContent;

    /**
     * 前端流程设计页使用的四步流程 JSON 字符串。
     * 当前阶段与 scriptContent 共用存储，后续再独立建模。
     */
    private String processData;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
