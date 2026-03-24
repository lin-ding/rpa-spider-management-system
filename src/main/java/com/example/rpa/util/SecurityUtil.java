package com.example.rpa.util;

import com.example.rpa.entity.SysUser;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 * 提供权限验证和用户信息获取功能
 */
@Component
public class SecurityUtil {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new BusinessException("未获取到用户信息");
    }
    
    /**
     * 获取当前登录用户信息
     */
    public SysUser getCurrentUser() {
        Long userId = getCurrentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    /**
     * 检查当前用户是否为管理员
     * 这里假设用户名为"admin"的用户为管理员
     * 实际项目中可以根据角色或权限来判断
     */
    public boolean isAdmin() {
        try {
            SysUser currentUser = getCurrentUser();
            return "admin".equals(currentUser.getUsername());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证当前用户是否为管理员，如果不是则抛出异常
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException("权限不足，仅管理员可操作");
        }
    }
    
    /**
     * 验证当前用户是否有权限操作指定用户
     * 管理员可以操作所有用户，普通用户只能操作自己的数据
     */
    public void checkUserPermission(Long targetUserId) {
        if (isAdmin()) {
            return; // 管理员有所有权限
        }
        
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(targetUserId)) {
            throw new BusinessException("权限不足，只能操作自己的数据");
        }
    }
}