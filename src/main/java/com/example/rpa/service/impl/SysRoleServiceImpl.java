package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.SysRole;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysRoleMapper;
import com.example.rpa.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

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
    public void deleteRole(Long id) {
        getRoleById(id);
        sysRoleMapper.deleteById(id);
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
}
