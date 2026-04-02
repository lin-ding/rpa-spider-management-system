package com.example.rpa.controller;

import com.example.rpa.common.Result;
import com.example.rpa.entity.SysMenu;
import com.example.rpa.service.SysMenuService;
import com.example.rpa.vo.MenuTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单管理 Controller
 */
@RestController
@RequestMapping("/system/menu")
@Tag(name = "菜单管理", description = "提供系统菜单树查询、菜单详情、菜单增删改和菜单名称唯一性校验接口")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 查询菜单树
     */
    @GetMapping("/tree")
    @Operation(summary = "查询菜单树", description = "查询系统全部菜单并组装为树形结构，供菜单管理页面展示")
    public Result<List<MenuTreeVO>> getMenuTree() {
        List<MenuTreeVO> menuTree = sysMenuService.getMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 根据 ID 查询菜单
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询菜单详情", description = "根据菜单主键 ID 查询单个菜单的详细信息")
    public Result<SysMenu> getMenuById(@Parameter(description = "菜单主键 ID", required = true)
                                       @PathVariable Long id) {
        SysMenu menu = sysMenuService.getMenuById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @Operation(summary = "新增菜单", description = "新增系统菜单、目录或按钮权限配置")
    public Result<Void> add(@RequestBody SysMenu menu) {
        sysMenuService.addMenu(menu);
        return Result.success();
    }

    /**
     * 修改菜单
     */
    @PutMapping
    @Operation(summary = "修改菜单", description = "修改已有菜单的名称、路径、权限标识、显示状态等信息")
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateMenu(menu);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单", description = "根据菜单主键 ID 删除菜单配置")
    public Result<Void> delete(@Parameter(description = "菜单主键 ID", required = true)
                               @PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Result.success();
    }

    /**
     * 检查菜单名称是否唯一
     */
    @GetMapping("/checkMenuName")
    @Operation(summary = "校验菜单名称唯一性", description = "校验同级菜单名称是否可用，返回 true 表示未被占用")
    public Result<Boolean> checkMenuName(SysMenu menu) {
        boolean unique = sysMenuService.checkMenuNameUnique(menu);
        return Result.success(unique);
    }
}
