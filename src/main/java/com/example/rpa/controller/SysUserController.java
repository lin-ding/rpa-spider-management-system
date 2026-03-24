package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.entity.SysUser;
import com.example.rpa.service.SysUserService;
import com.example.rpa.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统用户管理 Controller
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<Page<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            SysUser user) {
        // 只有管理员可以查看用户列表
        securityUtil.requireAdmin();
        Page<SysUser> page = sysUserService.getUserPage(current, size, user);
        return Result.success(page);
    }

    /**
     * 根据 ID 查询用户
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        SysUser user = sysUserService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        // 只有管理员可以新增用户
        securityUtil.requireAdmin();
        sysUserService.addUser(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        // 只有管理员可以修改用户信息
        securityUtil.requireAdmin();
        sysUserService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 只有管理员可以删除用户
        securityUtil.requireAdmin();
        sysUserService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    public Result<Void> resetPassword(@RequestParam Long userId, @RequestParam String password) {
        // 只有管理员可以重置密码
        securityUtil.requireAdmin();
        sysUserService.resetPassword(userId, password);
        return Result.success();
    }

    /**
     * 检查用户名是否唯一
     */
    @GetMapping("/checkUsername")
    public Result<Boolean> checkUsername(SysUser user) {
        boolean unique = sysUserService.checkUsernameUnique(user);
        return Result.success(unique);
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        // 只有管理员可以切换用户状态
        securityUtil.requireAdmin();
        sysUserService.toggleUserStatus(id);
        return Result.success();
    }

    /**
     * 根据用户名关键字搜索用户
     */
    @GetMapping("/search")
    public Result<List<SysUser>> searchUsers(@RequestParam String keyword) {
        // 只有管理员可以搜索用户
        securityUtil.requireAdmin();
        List<SysUser> users = sysUserService.searchUsersByUsername(keyword);
        return Result.success(users);
    }
}
