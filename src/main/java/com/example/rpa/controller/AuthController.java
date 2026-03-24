package com.example.rpa.controller;

import com.example.rpa.common.Result;
import com.example.rpa.dto.LoginRequest;
import com.example.rpa.dto.ChangePasswordRequest;
import com.example.rpa.dto.UpdateUserInfoRequest;
import com.example.rpa.service.AuthService;
import com.example.rpa.vo.LoginResponse;
import com.example.rpa.vo.MenuTreeVO;
import com.example.rpa.vo.UserInfoVO;
import com.example.rpa.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录认证 Controller
 */
@RestController
@RequestMapping("/user")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo(@RequestHeader("Authorization") String token) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        UserInfoVO userInfo = authService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 获取用户的菜单树
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuTreeVO>> getMenuTree(@RequestHeader("Authorization") String token) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<MenuTreeVO> menuTree = authService.getMenuTree(userId);
        return Result.success(menuTree);
    }

    /**
     * 验证 Token
     */
    @GetMapping("/token/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        boolean valid = authService.validateToken(token.replace("Bearer ", ""));
        return Result.success(valid);
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestHeader("Authorization") String token, @Valid @RequestBody UpdateUserInfoRequest request) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        authService.updateUserInfo(userId, request.getRealName(), request.getEmail(), request.getPhone());
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String token, @Valid @RequestBody ChangePasswordRequest request) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
