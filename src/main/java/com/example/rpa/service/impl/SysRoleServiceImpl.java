package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddRoleRequest;
import com.example.rpa.dto.AssignPermissionRequest;
import com.example.rpa.dto.RoleQueryRequest;
import com.example.rpa.dto.UpdateRoleRequest;
import com.example.rpa.entity.SysRole;
import com.example.rpa.entity.SysRoleResource;
import com.example.rpa.entity.SysUserRole;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysRoleMapper;
import com.example.rpa.mapper.SysRoleResourceMapper;
import com.example.rpa.mapper.SysUserRoleMapper;
import com.example.rpa.service.SysRoleService;
import com.example.rpa.vo.RoleListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleResourceMapper sysRoleResourceMapper;

    @Override
    public Page<SysRole> getRolePage(Integer current, Integer size, SysRole role) {
        Page<SysRole> page = new Page<>(current, size);
        
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(StringUtils.hasText(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
                   .eq(StringUtils.hasText(role.getRoleCode()), SysRole::getRoleCode, role.getRoleCode())
                   .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus());
        }
        
        return sysRoleMapper.selectPage(page, wrapper);
    }

    @Override
    public Page<RoleListItemVO> getRolePageWithUserCount(RoleQueryRequest request) {
        Page<SysRole> page = new Page<>(request.getCurrent(), request.getSize());
        
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getRoleName()), SysRole::getRoleName, request.getRoleName())
               .like(StringUtils.hasText(request.getRoleCode()), SysRole::getRoleCode, request.getRoleCode())
               .eq(request.getStatus() != null, SysRole::getStatus, request.getStatus())
               .orderByDesc(SysRole::getCreateTime);
        
        Page<SysRole> rolePage = sysRoleMapper.selectPage(page, wrapper);
        
        Page<RoleListItemVO> resultPage = new Page<>(rolePage.getCurrent(), rolePage.getSize());
        resultPage.setTotal(rolePage.getTotal());
        
        List<RoleListItemVO> voList = new ArrayList<>();
        for (SysRole role : rolePage.getRecords()) {
            RoleListItemVO vo = new RoleListItemVO();
            vo.setId(role.getId());
            vo.setRoleCode(role.getRoleCode());
            vo.setRoleName(role.getRoleName());
            vo.setDescription(role.getDescription());
            vo.setStatus(role.getStatus());
            vo.setStatusDesc(role.getStatus() == 1 ? "正常" : "禁用");
            vo.setCreateTime(role.getCreateTime());
            
            LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
            userRoleWrapper.eq(SysUserRole::getRoleId, role.getId());
            Long userCount = sysUserRoleMapper.selectCount(userRoleWrapper);
            vo.setUserCount(userCount);
            
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        
        return resultPage;
    }

    @Override
    public List<SysRole> getAllRoles() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public SysRole getRoleById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRole role) {
        if (!checkRoleCodeUnique(role)) {
            throw new BusinessException("角色编码已存在");
        }
        
        Long maxId = sysRoleMapper.selectMaxId();
        role.setId(maxId + 1);
        
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setDeleted(0);
        
        sysRoleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(AddRoleRequest request) {
        SysRole checkRole = new SysRole();
        checkRole.setRoleCode(request.getRoleCode());
        if (!checkRoleCodeUnique(checkRole)) {
            throw new BusinessException("角色编码已存在");
        }
        
        Long maxId = sysRoleMapper.selectMaxId();
        
        SysRole role = new SysRole();
        role.setId(maxId + 1);
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setDeleted(0);
        
        sysRoleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        SysRole existRole = getRoleById(role.getId());
        
        if (!existRole.getRoleCode().equals(role.getRoleCode()) && !checkRoleCodeUnique(role)) {
            throw new BusinessException("角色编码已存在");
        }
        
        role.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(UpdateRoleRequest request) {
        SysRole existRole = getRoleById(request.getId());
        
        SysRole role = new SysRole();
        role.setId(request.getId());
        role.setRoleCode(existRole.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus());
        role.setCreateTime(existRole.getCreateTime());
        role.setUpdateTime(LocalDateTime.now());
        
        sysRoleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        getRoleById(id);
        
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getRoleId, id);
        Long userCount = sysUserRoleMapper.selectCount(wrapper);
        if (userCount > 0) {
            throw new BusinessException("该角色已分配给用户，无法删除");
        }
        
        sysRoleMapper.physicalDeleteById(id);
    }

    @Override
    public boolean checkRoleCodeUnique(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, role.getRoleCode());
        if (role.getId() != null) {
            wrapper.ne(SysRole::getId, role.getId());
        }
        Long count = sysRoleMapper.selectCount(wrapper);
        return count == 0;
    }

    @Override
    public List<Long> getRoleResourceIds(Long roleId) {
        return sysRoleResourceMapper.selectResourceIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(AssignPermissionRequest request) {
        getRoleById(request.getRoleId());
        
        sysRoleResourceMapper.deleteByRoleId(request.getRoleId());
        
        if (request.getResourceIds() != null && !request.getResourceIds().isEmpty()) {
            Long maxId = sysRoleResourceMapper.selectMaxId();
            long id = maxId + 1;
            
            for (Long resourceId : request.getResourceIds()) {
                SysRoleResource roleResource = new SysRoleResource();
                roleResource.setId(id++);
                roleResource.setRoleId(request.getRoleId());
                roleResource.setResourceId(resourceId);
                roleResource.setCreateTime(LocalDateTime.now());
                sysRoleResourceMapper.insert(roleResource);
            }
        }
    }
}
