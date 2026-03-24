package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.entity.SysUser;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysUserMapper;
import com.example.rpa.service.SysUserService;
import com.example.rpa.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil;

    @Override
    public Page<SysUser> getUserPage(Integer current, Integer size, SysUser user) {
        Page<SysUser> page = new Page<>(current, size);
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (user != null) {
            wrapper.eq(StringUtils.hasText(user.getUsername()), SysUser::getUsername, user.getUsername())
                   .eq(StringUtils.hasText(user.getRealName()), SysUser::getRealName, user.getRealName())
                   .eq(user.getStatus() != null, SysUser::getStatus, user.getStatus());
        }
        
        return sysUserMapper.selectPage(page, wrapper);
    }

    @Override
    public SysUser getUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        return sysUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUser user) {
        if (!checkUsernameUnique(user)) {
            throw new BusinessException("用户名已存在");
        }
        
        // 验证密码强度
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (!passwordUtil.validatePasswordStrength(user.getPassword())) {
                throw new BusinessException("密码强度不符合要求：" + passwordUtil.getPasswordStrengthHint());
            }
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 生成随机密码
            user.setPassword(passwordEncoder.encode(passwordUtil.generateRandomPassword()));
        }
        
        // 设置默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        sysUserMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        SysUser existUser = getUserById(user.getId());
        
        if (!existUser.getUsername().equals(user.getUsername()) && !checkUsernameUnique(user)) {
            throw new BusinessException("用户名已存在");
        }
        
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        getUserById(id);
        sysUserMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public boolean checkUsernameUnique(SysUser user) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, user.getUsername());
        if (user.getId() != null) {
            wrapper.ne(SysUser::getId, user.getId());
        }
        Long count = sysUserMapper.selectCount(wrapper);
        return count == 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleUserStatus(Long userId) {
        SysUser user = getUserById(userId);
        
        // 切换状态：1（启用）<-> 0（禁用）
        Integer newStatus = user.getStatus() == 1 ? 0 : 1;
        user.setStatus(newStatus);
        user.setUpdateTime(LocalDateTime.now());
        
        sysUserMapper.updateById(user);
    }

    @Override
    public List<SysUser> searchUsersByUsername(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BusinessException("搜索关键字不能为空");
        }
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysUser::getUsername, keyword.trim());
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        return sysUserMapper.selectList(wrapper);
    }
}
