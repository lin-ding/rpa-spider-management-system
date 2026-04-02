package com.example.rpa.service;

import com.example.rpa.entity.SysMenu;
import com.example.rpa.vo.MenuTreeVO;

import java.util.List;

/**
 * 系统菜单 Service
 */
public interface SysMenuService {

    /**
     * 查询菜单树
     */
    List<MenuTreeVO> getMenuTree();

    /**
     * 根据 ID 查询菜单
     */
    SysMenu getMenuById(Long id);

    /**
     * 新增菜单
     */
    void addMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    void updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);

    /**
     * 检查菜单名称是否唯一
     */
    boolean checkMenuNameUnique(SysMenu menu);
}
