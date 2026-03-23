package com.example.rpa.controller;

import com.example.rpa.common.Result;
import com.example.rpa.entity.SysMenu;
import com.example.rpa.service.SysMenuService;
import com.example.rpa.vo.MenuTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单管理 Controller
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 查询菜单树
     */
    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> getMenuTree() {
        List<MenuTreeVO> menuTree = sysMenuService.getMenuTree();
        return Result.success(menuTree);
    }

    /**
     * 根据 ID 查询菜单
     */
    @GetMapping("/{id}")
    public Result<SysMenu> getMenuById(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getMenuById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        sysMenuService.addMenu(menu);
        return Result.success();
    }

    /**
     * 修改菜单
     */
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateMenu(menu);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Result.success();
    }

    /**
     * 检查菜单名称是否唯一
     */
    @GetMapping("/checkMenuName")
    public Result<Boolean> checkMenuName(SysMenu menu) {
        boolean unique = sysMenuService.checkMenuNameUnique(menu);
        return Result.success(unique);
    }
}
