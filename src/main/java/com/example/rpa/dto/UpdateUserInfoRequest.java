package com.example.rpa.dto;

import lombok.Data;

/**
 * 修改个人信息请求 DTO
 */
@Data
public class UpdateUserInfoRequest {

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;
}