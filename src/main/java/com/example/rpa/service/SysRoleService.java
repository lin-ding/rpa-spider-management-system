package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.SysRole;

import java.util.List;

/**
 * 系统角色 Service
 */
public interface SysRoleService {

    /**
     * 分页查询角色列表
     */
    Page<SysRole> getRolePage(Integer current, Integer size, SysRole role);

    /**
     * 查询所有角色
     */
    List<SysRole> getAllRoles();

    /**
     * 根据 ID 查询角色
     */
    SysRole getRoleById(Long id);

    /**
     * 新增角色
     */
    void addRole(SysRole role);

    /**
     * 修改角色
     */
    void updateRole(SysRole role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 检查角色编码是否唯一
     */
    boolean checkRoleCodeUnique(SysRole role);
}
