package com.example.rpa.service.impl;

import com.example.rpa.dto.LoginRequest;
import com.example.rpa.entity.SysUser;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysUserMapper;
import com.example.rpa.service.AuthService;
import com.example.rpa.util.JwtUtil;
import com.example.rpa.vo.LoginResponse;
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
        // 1. 校验用户名是否存在
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 密码加密比对（使用BCrypt）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 账号状态校验（是否启用）
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 5. 构建并返回用户信息
        // 设置登录成功消息
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
    public void logout(String token) {}

    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        return new UserInfoVO();
    }

    @Override
    public List<MenuTreeVO> getMenuTree(Long userId) {
        return new ArrayList<>();
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}