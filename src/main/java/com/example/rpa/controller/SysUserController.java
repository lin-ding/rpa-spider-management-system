package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.dto.AddUserRequest;
import com.example.rpa.dto.ResetPasswordRequest;
import com.example.rpa.dto.UpdateUserRequest;
import com.example.rpa.dto.UserQueryRequest;
import com.example.rpa.entity.SysUser;
import com.example.rpa.service.SysUserService;
import com.example.rpa.util.SecurityUtil;
import com.example.rpa.vo.UserDetailVO;
import com.example.rpa.vo.UserListItemVO;
import com.example.rpa.vo.UserListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "提供用户分页查询、详情查看、增删改查、状态切换、密码重置和用户名校验接口")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/list")
    @RequireAdmin("查询用户列表")
    @Operation(summary = "分页查询用户列表", description = "分页查询用户基础信息，并按统一列表结构返回记录和分页信息")
    public Result<UserListVO<SysUser>> getUserList(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            SysUser user) {
        Page<SysUser> page = sysUserService.getUserPage(current, size, user);
        
        UserListVO<SysUser> vo = new UserListVO<>(
            page.getRecords(),
            page.getTotal(),
            page.getCurrent(),
            page.getSize()
        );
        
        return Result.success(vo);
    }

    @GetMapping("/page")
    @RequireAdmin("查询用户列表")
    @Operation(summary = "分页查询用户 Page 对象", description = "分页查询用户基础信息，并直接返回 MyBatis-Plus 的分页对象")
    public Result<Page<SysUser>> getUserPage(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            SysUser user) {
        Page<SysUser> page = sysUserService.getUserPage(current, size, user);
        return Result.success(page);
    }

    @GetMapping("/query")
    @RequireAdmin("查询用户列表")
    @Operation(summary = "分页查询用户及角色信息", description = "分页查询用户列表，并附带每个用户关联的角色信息")
    public Result<Page<UserListItemVO>> queryUserList(UserQueryRequest request) {
        Page<UserListItemVO> page = sysUserService.getUserPageWithRoles(request);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户基础信息", description = "根据用户主键 ID 查询用户基础信息")
    public Result<SysUser> getUserById(@Parameter(description = "用户主键 ID", required = true)
                                       @PathVariable Long id) {
        SysUser user = sysUserService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/{id}/detail")
    @RequireAdmin("查看用户详情")
    @Operation(summary = "查询用户详情", description = "根据用户主键 ID 查询用户详情，并返回其关联角色信息")
    public Result<UserDetailVO> getUserDetail(@Parameter(description = "用户主键 ID", required = true)
                                              @PathVariable Long id) {
        UserDetailVO detail = sysUserService.getUserDetailById(id);
        return Result.success(detail);
    }

    @PostMapping
    @RequireAdmin("新增用户")
    @Operation(summary = "新增用户", description = "新增一个系统用户，并可同步分配单个角色或多个角色")
    public Result<Void> add(@Valid @RequestBody AddUserRequest request) {
        sysUserService.addUser(request);
        return Result.success();
    }

    @PutMapping
    @RequireAdmin("修改用户")
    @Operation(summary = "修改用户", description = "根据请求体中的用户 ID 修改用户资料、状态和角色分配信息")
    public Result<Void> update(@Valid @RequestBody UpdateUserRequest request) {
        sysUserService.updateUser(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改用户")
    @Operation(summary = "按路径修改用户", description = "根据路径中的用户 ID 修改用户信息，适用于 REST 风格更新接口")
    public Result<Void> updateById(@Parameter(description = "用户主键 ID", required = true)
                                   @PathVariable Long id,
                                   @Valid @RequestBody UpdateUserRequest request) {
        request.setId(id);
        sysUserService.updateUser(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除用户")
    @Operation(summary = "删除用户", description = "根据用户主键 ID 删除用户，系统管理员账号不允许删除")
    public Result<Void> delete(@Parameter(description = "用户主键 ID", required = true)
                               @PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/resetPwd")
    @RequireAdmin("重置密码")
    @Operation(summary = "重置用户密码", description = "按管理员权限重置指定用户的登录密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(request);
        return Result.success();
    }

    @GetMapping("/checkUsername")
    @Operation(summary = "校验用户名唯一性", description = "校验用户名是否已存在，返回 true 表示用户名可用")
    public Result<Boolean> checkUsername(SysUser user) {
        boolean unique = sysUserService.checkUsernameUnique(user);
        return Result.success(unique);
    }

    @PutMapping("/{id}/status")
    @RequireAdmin("切换用户状态")
    @Operation(summary = "切换用户状态", description = "将指定用户在启用和禁用状态之间进行切换")
    public Result<Void> toggleStatus(@Parameter(description = "用户主键 ID", required = true)
                                     @PathVariable Long id) {
        sysUserService.toggleUserStatus(id);
        return Result.success();
    }

    @GetMapping("/search")
    @RequireAdmin("搜索用户")
    @Operation(summary = "按用户名关键字搜索用户", description = "根据用户名关键字模糊搜索用户列表")
    public Result<List<SysUser>> searchUsers(@Parameter(description = "用户名关键字", required = true)
                                             @RequestParam String keyword) {
        List<SysUser> users = sysUserService.searchUsersByUsername(keyword);
        return Result.success(users);
    }
}
