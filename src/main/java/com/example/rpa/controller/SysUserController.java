package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.entity.SysUser;
import com.example.rpa.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户管理 Controller
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public Result<Page<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            SysUser user) {
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
        sysUserService.addUser(user);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    public Result<Void> resetPassword(@RequestParam Long userId, @RequestParam String password) {
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
}
