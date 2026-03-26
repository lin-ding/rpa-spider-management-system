package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddUserRequest;
import com.example.rpa.dto.ResetPasswordRequest;
import com.example.rpa.dto.UpdateUserRequest;
import com.example.rpa.dto.UserQueryRequest;
import com.example.rpa.entity.SysRole;
import com.example.rpa.entity.SysUser;
import com.example.rpa.entity.SysUserRole;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysRoleMapper;
import com.example.rpa.mapper.SysUserMapper;
import com.example.rpa.mapper.SysUserRoleMapper;
import com.example.rpa.service.SysUserService;
import com.example.rpa.util.PasswordUtil;
import com.example.rpa.vo.UserDetailVO;
import com.example.rpa.vo.UserListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordUtil passwordUtil;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

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
    public Page<UserListItemVO> getUserPageWithRoles(UserQueryRequest request) {
        Page<SysUser> page = new Page<>(request.getCurrent(), request.getSize());
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(request.getUsername()), SysUser::getUsername, request.getUsername())
               .like(StringUtils.hasText(request.getRealName()), SysUser::getRealName, request.getRealName())
               .eq(request.getStatus() != null, SysUser::getStatus, request.getStatus())
               .orderByDesc(SysUser::getCreateTime);
        
        if (request.getRoleId() != null) {
            List<Long> userIds = sysUserRoleMapper.selectUserIdsByRoleId(request.getRoleId());
            if (userIds.isEmpty()) {
                Page<UserListItemVO> emptyPage = new Page<>(request.getCurrent(), request.getSize());
                emptyPage.setRecords(new ArrayList<>());
                emptyPage.setTotal(0);
                return emptyPage;
            }
            wrapper.in(SysUser::getId, userIds);
        }
        
        Page<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);
        
        Page<UserListItemVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize());
        resultPage.setTotal(userPage.getTotal());
        
        List<UserListItemVO> voList = new ArrayList<>();
        for (SysUser user : userPage.getRecords()) {
            UserListItemVO vo = new UserListItemVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setRealName(user.getRealName());
            vo.setEmail(user.getEmail());
            vo.setPhone(user.getPhone());
            vo.setStatus(user.getStatus());
            vo.setStatusDesc(user.getStatus() == 1 ? "正常" : "禁用");
            vo.setCreateTime(user.getCreateTime());
            
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());
            List<UserListItemVO.RoleInfo> roleInfos = new ArrayList<>();
            for (SysRole role : roles) {
                UserListItemVO.RoleInfo roleInfo = new UserListItemVO.RoleInfo();
                roleInfo.setId(role.getId());
                roleInfo.setRoleName(role.getRoleName());
                roleInfo.setRoleCode(role.getRoleCode());
                roleInfos.add(roleInfo);
            }
            vo.setRoles(roleInfos);
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        
        return resultPage;
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
    public UserDetailVO getUserDetailById(Long id) {
        SysUser user = getUserById(id);
        
        UserDetailVO vo = new UserDetailVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        vo.setStatusDesc(user.getStatus() == 1 ? "正常" : "禁用");
        vo.setDeptId(user.getDeptId());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(id);
        List<UserDetailVO.RoleInfo> roleInfos = new ArrayList<>();
        for (SysRole role : roles) {
            UserDetailVO.RoleInfo roleInfo = new UserDetailVO.RoleInfo();
            roleInfo.setId(role.getId());
            roleInfo.setRoleName(role.getRoleName());
            roleInfo.setRoleCode(role.getRoleCode());
            roleInfos.add(roleInfo);
        }
        vo.setRoles(roleInfos);
        
        return vo;
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
    public void addUser(AddUserRequest request) {
        if (!checkUsernameUnique(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setDeptId(request.getDeptId());
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        sysUserMapper.insert(user);
        
        Long roleId = request.getRoleId();
        if (roleId != null) {
            Long maxUserRoleId = sysUserRoleMapper.selectMaxId();
            SysUserRole userRole = new SysUserRole();
            userRole.setId(maxUserRoleId + 1);
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }
        
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Long maxUserRoleId = sysUserRoleMapper.selectMaxId();
            long id = maxUserRoleId + 1;
            for (Long rid : request.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setId(id++);
                userRole.setUserId(user.getId());
                userRole.setRoleId(rid);
                userRole.setCreateTime(LocalDateTime.now());
                sysUserRoleMapper.insert(userRole);
            }
        }
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
    public void updateUser(UpdateUserRequest request) {
        SysUser existUser = getUserById(request.getId());
        
        existUser.setRealName(request.getRealName());
        existUser.setEmail(request.getEmail());
        existUser.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            existUser.setStatus(request.getStatus());
        }
        existUser.setDeptId(request.getDeptId());
        existUser.setUpdateTime(LocalDateTime.now());
        
        sysUserMapper.updateById(existUser);
        
        Long roleId = request.getRoleId();
        
        if (roleId != null && roleId > 0) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, request.getId()));
            
            Long maxUserRoleId = sysUserRoleMapper.selectMaxId();
            SysUserRole userRole = new SysUserRole();
            userRole.setId(maxUserRoleId + 1);
            userRole.setUserId(request.getId());
            userRole.setRoleId(roleId);
            userRole.setCreateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(userRole);
        }
        
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, request.getId()));
            
            Long maxUserRoleId = sysUserRoleMapper.selectMaxId();
            long id = maxUserRoleId + 1;
            for (Long rid : request.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setId(id++);
                userRole.setUserId(request.getId());
                userRole.setRoleId(rid);
                userRole.setCreateTime(LocalDateTime.now());
                sysUserRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        SysUser user = getUserById(id);
        
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除系统管理员账号");
        }
        
        sysUserMapper.deleteUserRolesByUserId(id);
        sysUserMapper.physicalDeleteById(id);
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
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(ResetPasswordRequest request) {
        if (!passwordUtil.validatePasswordStrength(request.getNewPassword())) {
            throw new BusinessException("密码强度不符合要求：" + passwordUtil.getPasswordStrengthHint());
        }
        
        SysUser user = getUserById(request.getUserId());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }

    @Override
    public boolean checkUsernameUnique(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        Long count = sysUserMapper.selectCount(wrapper);
        return count == 0;
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
