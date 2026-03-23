package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.common.Result;
import com.example.rpa.entity.SysRole;
import com.example.rpa.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统角色管理 Controller
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public Result<Page<SysRole>> getRolePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            SysRole role) {
        Page<SysRole> page = sysRoleService.getRolePage(current, size, role);
        return Result.success(page);
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/all")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }

    /**
     * 根据 ID 查询角色
     */
    @GetMapping("/{id}")
    public Result<SysRole> getRoleById(@PathVariable Long id) {
        SysRole role = sysRoleService.getRoleById(id);
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysRole role) {
        sysRoleService.addRole(role);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        sysRoleService.updateRole(role);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 检查角色编码是否唯一
     */
    @GetMapping("/checkRoleCode")
    public Result<Boolean> checkRoleCode(SysRole role) {
        boolean unique = sysRoleService.checkRoleCodeUnique(role);
        return Result.success(unique);
    }
}
