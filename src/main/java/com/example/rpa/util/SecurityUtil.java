package com.example.rpa.util;

import com.example.rpa.entity.SysRole;
import com.example.rpa.entity.SysUser;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysResourceMapper;
import com.example.rpa.mapper.SysRoleMapper;
import com.example.rpa.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

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

    @Autowired
    private SysResourceMapper sysResourceMapper;
    
    public Long getCurrentUserId() {
        String token = getCurrentToken();
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "登录已过期或无效，请重新登录");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        SysUser user = sysUserMapper.selectById(userId);
        validateActiveUser(user);
        return userId;
    }
    
    public SysUser getCurrentUser() {
        Long userId = getCurrentUserId();
        SysUser user = sysUserMapper.selectById(userId);
        validateActiveUser(user);
        return user;
    }
    
    public boolean isAdmin() {
        Long userId = getCurrentUserId();
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        log.info("用户ID: {}, 查询到的角色列表: {}", userId, roles);
        boolean isAdmin = roles.stream()
                .anyMatch(role -> "super_admin".equals(role.getRoleCode()));
        log.info("用户ID: {}, 是否为管理员: {}", userId, isAdmin);
        return isAdmin;
    }

    public boolean hasPermission(String resourceCode) {
        if (!StringUtils.hasText(resourceCode)) {
            return false;
        }

        Long userId = getCurrentUserId();
        if (isAdminByUserId(userId)) {
            return true;
        }

        Long count = sysResourceMapper.countPermissionByUserIdAndCode(userId, resourceCode);
        return count != null && count > 0;
    }

    public boolean hasAnyPermission(List<String> resourceCodes) {
        List<String> normalizedCodes = resourceCodes.stream()
                .filter(StringUtils::hasText)
                .toList();
        if (normalizedCodes.isEmpty()) {
            return false;
        }

        Long userId = getCurrentUserId();
        if (isAdminByUserId(userId)) {
            return true;
        }

        return normalizedCodes.stream().anyMatch(resourceCode -> {
            Long count = sysResourceMapper.countPermissionByUserIdAndCode(userId, resourceCode);
            return count != null && count > 0;
        });
    }
    
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(403, "权限不足，仅管理员可操作");
        }
    }
    
    public void checkUserPermission(Long targetUserId) {
        if (isAdmin()) {
            return;
        }
        
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(targetUserId)) {
            throw new BusinessException(403, "权限不足，只能操作自己的数据");
        }
    }

    private String getCurrentToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(401, "未获取到请求上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        throw new BusinessException(401, "未获取到用户信息");
    }

    private boolean isAdminByUserId(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        return roles.stream()
                .anyMatch(role -> "super_admin".equals(role.getRoleCode()));
    }

    private void validateActiveUser(SysUser user) {
        if (user == null) {
            throw new BusinessException(401, "用户不存在或已删除");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用，请联系管理员");
        }
    }
}
