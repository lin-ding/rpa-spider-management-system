package com.example.rpa.service.impl;

import com.example.rpa.dto.LoginRequest;
import com.example.rpa.entity.SysUser;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysUserMapper;
import com.example.rpa.service.AuthService;
import com.example.rpa.util.JwtUtil;
import com.example.rpa.vo.LoginResponse;
import com.example.rpa.vo.RoleInfoVO;
import com.example.rpa.vo.UserInfoVO;
import com.example.rpa.vo.MenuTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录 - 包含用户名校验、密码比对、状态检查、JWT生成
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        
        if (user == null) {
            SysUser deletedUser = sysUserMapper.selectByUsernameIncludeDeleted(request.getUsername());
            if (deletedUser != null && deletedUser.getDeleted() == 1) {
                throw new BusinessException("账号不存在");
            }
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(new ArrayList<>())
                .permissions(new ArrayList<>())
                .build();
    }


    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 构建用户信息VO
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        
        // 设置角色信息
        List<RoleInfoVO> roles = new ArrayList<>();
        RoleInfoVO role = new RoleInfoVO();
        
        // 判断是否为管理员（这里假设用户名为"admin"的用户为管理员）
        if ("admin".equals(user.getUsername())) {
            role.setRoleName("系统管理员");
            role.setRoleCode("admin");
            role.setDescription("系统管理员，拥有所有权限");
        } else {
            role.setRoleName("普通用户");
            role.setRoleCode("user");
            role.setDescription("普通用户，拥有基本权限");
        }
        
        roles.add(role);
        userInfo.setRoles(roles);
        
        return userInfo;
    }

    @Override
    public List<MenuTreeVO> getMenuTree(Long userId) {
        return new ArrayList<>();
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public void logout(String token) {
        // 使Token失效，加入黑名单
        jwtUtil.invalidateToken(token);
    }

    @Override
    public void updateUserInfo(Long userId, String realName, String email, String phone) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        
        sysUserMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }
}