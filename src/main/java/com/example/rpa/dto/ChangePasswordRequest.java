package com.example.rpa.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordRequest {

    /**
     * 原密码
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}