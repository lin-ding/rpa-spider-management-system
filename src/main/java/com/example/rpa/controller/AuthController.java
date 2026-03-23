package com.example.rpa.controller;

import com.example.rpa.common.Result;
import com.example.rpa.dto.LoginRequest;
import com.example.rpa.service.AuthService;
import com.example.rpa.vo.LoginResponse;
import com.example.rpa.vo.MenuTreeVO;
import com.example.rpa.vo.UserInfoVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录认证 Controller
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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
    @GetMapping("/user/info")
    public Result<UserInfoVO> getUserInfo(@RequestParam Long userId) {
        UserInfoVO userInfo = authService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 获取用户的菜单树
     */
    @GetMapping("/menu/tree")
    public Result<List<MenuTreeVO>> getMenuTree(@RequestParam Long userId) {
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
}
