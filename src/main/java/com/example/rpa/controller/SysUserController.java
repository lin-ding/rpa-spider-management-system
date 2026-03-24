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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SecurityUtil securityUtil;

    @GetMapping("/list")
    @RequireAdmin("查询用户列表")
    public Result<UserListVO<SysUser>> getUserList(
            @RequestParam(defaultValue = "1") Integer current,
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
    public Result<Page<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            SysUser user) {
        Page<SysUser> page = sysUserService.getUserPage(current, size, user);
        return Result.success(page);
    }

    @GetMapping("/query")
    @RequireAdmin("查询用户列表")
    public Result<Page<UserListItemVO>> queryUserList(UserQueryRequest request) {
        Page<UserListItemVO> page = sysUserService.getUserPageWithRoles(request);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/{id}/detail")
    @RequireAdmin("查看用户详情")
    public Result<UserDetailVO> getUserDetail(@PathVariable Long id) {
        UserDetailVO detail = sysUserService.getUserDetailById(id);
        return Result.success(detail);
    }

    @PostMapping
    @RequireAdmin("新增用户")
    public Result<Void> add(@Valid @RequestBody AddUserRequest request) {
        sysUserService.addUser(request);
        return Result.success();
    }

    @PutMapping
    @RequireAdmin("修改用户")
    public Result<Void> update(@Valid @RequestBody UpdateUserRequest request) {
        sysUserService.updateUser(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改用户")
    public Result<Void> updateById(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        request.setId(id);
        sysUserService.updateUser(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/resetPwd")
    @RequireAdmin("重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(request);
        return Result.success();
    }

    @GetMapping("/checkUsername")
    public Result<Boolean> checkUsername(SysUser user) {
        boolean unique = sysUserService.checkUsernameUnique(user);
        return Result.success(unique);
    }

    @PutMapping("/{id}/status")
    @RequireAdmin("切换用户状态")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        sysUserService.toggleUserStatus(id);
        return Result.success();
    }

    @GetMapping("/search")
    @RequireAdmin("搜索用户")
    public Result<List<SysUser>> searchUsers(@RequestParam String keyword) {
        List<SysUser> users = sysUserService.searchUsersByUsername(keyword);
        return Result.success(users);
    }
}
