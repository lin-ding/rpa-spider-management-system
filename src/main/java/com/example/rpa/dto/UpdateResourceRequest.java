package com.example.rpa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateResourceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "资源ID不能为空")
    private Long id;

    private Long parentId;

    @NotBlank(message = "资源编码不能为空")
    private String resourceCode;

    @NotBlank(message = "资源名称不能为空")
    private String resourceName;

    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    private String url;

    private String icon;

    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
