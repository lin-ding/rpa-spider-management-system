package com.example.rpa.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应 VO
 */
@Data
@Builder
public class LoginResponse {

    /**
     * Token
     */
    private String token;

    /**
     * Token 类型
     */
    private String tokenType;

    /**
     * 过期时间
     */
    private Long expiresIn;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色列表
     */
    private List<RoleInfoVO> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;
}
