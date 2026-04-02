package com.example.rpa.util;

import com.example.rpa.entity.SysRole;
import com.example.rpa.entity.SysUser;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysRoleMapper;
import com.example.rpa.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Component
public class SecurityUtil {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SysUserMapper sysUserMapper;
    
    @Autowired
    private SysRoleMapper sysRoleMapper;
    
    public Long getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new BusinessException("未获取到用户信息");
    }
    
    public SysUser getCurrentUser() {
        Long userId = getCurrentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    public boolean isAdmin() {
        try {
            Long userId = getCurrentUserId();
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
            log.info("用户ID: {}, 查询到的角色列表: {}", userId, roles);
            boolean isAdmin = roles.stream()
                    .anyMatch(role -> "super_admin".equals(role.getRoleCode()));
            log.info("用户ID: {}, 是否为管理员: {}", userId, isAdmin);
            return isAdmin;
        } catch (Exception e) {
            log.error("权限验证异常", e);
            return false;
        }
    }
    
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException("权限不足，仅管理员可操作");
        }
    }
    
    public void checkUserPermission(Long targetUserId) {
        if (isAdmin()) {
            return;
        }
        
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(targetUserId)) {
            throw new BusinessException("权限不足，只能操作自己的数据");
        }
    }
}