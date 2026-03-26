package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddRoleRequest;
import com.example.rpa.dto.AssignPermissionRequest;
import com.example.rpa.dto.RoleQueryRequest;
import com.example.rpa.dto.UpdateRoleRequest;
import com.example.rpa.entity.SysRole;
import com.example.rpa.vo.RoleListItemVO;

import java.util.List;

public interface SysRoleService {

    Page<SysRole> getRolePage(Integer current, Integer size, SysRole role);

    Page<RoleListItemVO> getRolePageWithUserCount(RoleQueryRequest request);

    List<SysRole> getAllRoles();

    SysRole getRoleById(Long id);

    void addRole(SysRole role);

    void addRole(AddRoleRequest request);

    void updateRole(SysRole role);

    void updateRole(UpdateRoleRequest request);

    void deleteRole(Long id);

    boolean checkRoleCodeUnique(SysRole role);

    List<Long> getRoleResourceIds(Long roleId);

    void assignPermissions(AssignPermissionRequest request);
}
