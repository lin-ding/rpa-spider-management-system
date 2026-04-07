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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录认证 Controller
 */
@RestController
@RequestMapping("/user")
@Tag(name = "认证管理", description = "提供登录、登出、Token 校验、当前用户信息和个人信息维护接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名和密码完成登录认证，成功后返回访问令牌、用户信息和角色权限信息")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "将当前访问令牌加入黑名单，使其立即失效")
    public Result<Void> logout(@Parameter(description = "Bearer Token，格式为 Bearer 空格加令牌字符串", required = true)
                               @RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "根据请求头中的 Token 解析当前登录用户，并返回个人基础信息")
    public Result<UserInfoVO> getUserInfo(@Parameter(description = "Bearer Token，格式为 Bearer 空格加令牌字符串", required = true)
                                          @RequestHeader("Authorization") String token) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        UserInfoVO userInfo = authService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 获取用户的菜单树
     */
    @GetMapping("/menu/tree")
    @Operation(summary = "获取当前用户菜单树", description = "根据当前登录用户查询其可访问的菜单树结构，用于前端动态菜单渲染")
    public Result<List<MenuTreeVO>> getMenuTree(@Parameter(description = "Bearer Token，格式为 Bearer 空格加令牌字符串", required = true)
                                                @RequestHeader("Authorization") String token) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<MenuTreeVO> menuTree = authService.getMenuTree(userId);
        return Result.success(menuTree);
    }

    /**
     * 验证 Token
     */
    @GetMapping("/token/validate")
    @Operation(summary = "校验访问令牌", description = "校验请求头中的 Token 是否有效、是否过期以及是否已被拉黑")
    public Result<Boolean> validateToken(@Parameter(description = "Bearer Token，格式为 Bearer 空格加令牌字符串", required = true)
                                         @RequestHeader("Authorization") String token) {
        boolean valid = authService.validateToken(token.replace("Bearer ", ""));
        return Result.success(valid);
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/update")
    @Operation(summary = "修改个人资料", description = "修改当前登录用户的真实姓名、邮箱和手机号等基础资料")
    public Result<Void> updateUserInfo(@Parameter(description = "Bearer Token，格式为 Bearer 空格加令牌字符串", required = true)
                                       @RequestHeader("Authorization") String token,
                                       @Valid @RequestBody UpdateUserInfoRequest request) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        authService.updateUserInfo(userId, request.getRealName(), request.getEmail(), request.getPhone());
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改个人密码", description = "校验当前用户旧密码后，将登录密码修改为新密码")
    public Result<Void> changePassword(@Parameter(description = "Bearer Token，格式为 Bearer 空格加令牌字符串", required = true)
                                       @RequestHeader("Authorization") String token,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        // 从token中解析userId
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        authService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }
}
