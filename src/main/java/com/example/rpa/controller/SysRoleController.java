package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.common.Result;
import com.example.rpa.dto.AddRoleRequest;
import com.example.rpa.dto.AssignPermissionRequest;
import com.example.rpa.dto.RoleQueryRequest;
import com.example.rpa.dto.UpdateRoleRequest;
import com.example.rpa.entity.SysRole;
import com.example.rpa.service.SysRoleService;
import com.example.rpa.vo.RoleListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/role")
@Tag(name = "角色管理", description = "提供角色分页查询、详情查看、增删改查、权限分配和角色编码唯一性校验接口")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/page")
    @RequireAdmin("查询角色列表")
    @Operation(summary = "分页查询角色", description = "按角色名称、编码、状态等条件分页查询角色基础信息")
    public Result<Page<SysRole>> getRolePage(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") Integer size,
            SysRole role) {
        Page<SysRole> page = sysRoleService.getRolePage(current, size, role);
        return Result.success(page);
    }

    @GetMapping("/query")
    @RequireAdmin("查询角色列表")
    @Operation(summary = "分页查询角色及用户数", description = "分页查询角色列表，并返回每个角色关联的用户数量")
    public Result<Page<RoleListItemVO>> queryRoleList(RoleQueryRequest request) {
        Page<RoleListItemVO> page = sysRoleService.getRolePageWithUserCount(request);
        return Result.success(page);
    }

    @GetMapping("/all")
    @RequireAdmin("查询所有角色")
    @Operation(summary = "查询全部角色", description = "查询系统中全部可用角色，常用于下拉选择或角色分配")
    public Result<List<SysRole>> getAllRoles() {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    @RequireAdmin("查询角色详情")
    @Operation(summary = "查询角色详情", description = "根据角色主键 ID 查询角色的详细信息")
    public Result<SysRole> getRoleById(@Parameter(description = "角色主键 ID", required = true)
                                       @PathVariable Long id) {
        SysRole role = sysRoleService.getRoleById(id);
        return Result.success(role);
    }

    @PostMapping
    @RequireAdmin("新增角色")
    @Operation(summary = "新增角色", description = "新增一条角色数据，保存角色名称、编码、状态和描述信息")
    public Result<Void> add(@Valid @RequestBody AddRoleRequest request) {
        sysRoleService.addRole(request);
        return Result.success();
    }

    @PutMapping
    @RequireAdmin("修改角色")
    @Operation(summary = "修改角色", description = "根据请求体中的角色 ID 修改角色名称、描述和状态")
    public Result<Void> update(@Valid @RequestBody UpdateRoleRequest request) {
        sysRoleService.updateRole(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改角色")
    @Operation(summary = "按路径修改角色", description = "根据路径中的角色 ID 修改角色信息，适用于 REST 风格更新接口")
    public Result<Void> updateById(@Parameter(description = "角色主键 ID", required = true)
                                   @PathVariable Long id,
                                   @Valid @RequestBody UpdateRoleRequest request) {
        request.setId(id);
        sysRoleService.updateRole(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除角色")
    @Operation(summary = "删除角色", description = "根据角色主键 ID 删除角色信息")
    public Result<Void> delete(@Parameter(description = "角色主键 ID", required = true)
                               @PathVariable Long id) {
        log.info("删除角色请求，接收到的ID: {}", id);
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("/checkRoleCode")
    @RequireAdmin("检查角色编码")
    @Operation(summary = "校验角色编码唯一性", description = "校验角色编码是否已存在，返回 true 表示编码可用")
    public Result<Boolean> checkRoleCode(SysRole role) {
        boolean unique = sysRoleService.checkRoleCodeUnique(role);
        return Result.success(unique);
    }

    @GetMapping("/{id}/resources")
    @RequireAdmin("查询角色权限")
    @Operation(summary = "查询角色已分配资源", description = "根据角色主键 ID 查询当前角色已分配的资源 ID 列表")
    public Result<List<Long>> getRoleResources(@PathVariable Long id) {
        List<Long> resourceIds = sysRoleService.getRoleResourceIds(id);
        return Result.success(resourceIds);
    }

    @PostMapping("/assignPermissions")
    @RequireAdmin("分配权限")
    @Operation(summary = "为角色分配权限", description = "为指定角色分配资源权限，保存角色与资源的关联关系")
    public Result<Void> assignPermissions(@Valid @RequestBody AssignPermissionRequest request) {
        sysRoleService.assignPermissions(request);
        return Result.success();
    }
}
