package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.dto.AddRoleRequest;
import com.example.rpa.dto.RoleQueryRequest;
import com.example.rpa.dto.UpdateRoleRequest;
import com.example.rpa.entity.SysRole;
import com.example.rpa.service.SysRoleService;
import com.example.rpa.vo.RoleListItemVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/page")
    @RequireAdmin("查询角色列表")
    public Result<Page<SysRole>> getRolePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            SysRole role) {
        Page<SysRole> page = sysRoleService.getRolePage(current, size, role);
        return Result.success(page);
    }

    @GetMapping("/query")
    @RequireAdmin("查询角色列表")
    public Result<Page<RoleListItemVO>> queryRoleList(RoleQueryRequest request) {
        Page<RoleListItemVO> page = sysRoleService.getRolePageWithUserCount(request);
        return Result.success(page);
    }

    @GetMapping("/all")
    @RequireAdmin("查询所有角色")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    @RequireAdmin("查询角色详情")
    public Result<SysRole> getRoleById(@PathVariable Long id) {
        SysRole role = sysRoleService.getRoleById(id);
        return Result.success(role);
    }

    @PostMapping
    @RequireAdmin("新增角色")
    public Result<Void> add(@Valid @RequestBody AddRoleRequest request) {
        sysRoleService.addRole(request);
        return Result.success();
    }

    @PutMapping
    @RequireAdmin("修改角色")
    public Result<Void> update(@Valid @RequestBody UpdateRoleRequest request) {
        sysRoleService.updateRole(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改角色")
    public Result<Void> updateById(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        request.setId(id);
        sysRoleService.updateRole(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("/checkRoleCode")
    @RequireAdmin("检查角色编码")
    public Result<Boolean> checkRoleCode(SysRole role) {
        boolean unique = sysRoleService.checkRoleCodeUnique(role);
        return Result.success(unique);
    }
}
