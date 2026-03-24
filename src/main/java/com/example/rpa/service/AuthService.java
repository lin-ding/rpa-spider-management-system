package com.example.rpa.service;

import com.example.rpa.dto.LoginRequest;
import com.example.rpa.vo.LoginResponse;
import com.example.rpa.vo.MenuTreeVO;
import com.example.rpa.vo.UserInfoVO;

import java.util.List;

/**
 * 登录认证 Service
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出（使Token失效）
     */
    void logout(String token);

    /**
     * 获取当前登录用户信息
     */
    UserInfoVO getCurrentUserInfo(Long userId);

    /**
     * 获取用户的菜单树
     */
    List<MenuTreeVO> getMenuTree(Long userId);

    /**
     * 验证 Token
     */
    boolean validateToken(String token);

    /**
     * 修改个人信息
     */
    void updateUserInfo(Long userId, String realName, String email, String phone);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
