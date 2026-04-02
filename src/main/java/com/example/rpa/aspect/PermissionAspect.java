package com.example.rpa.aspect;

import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限验证切面
 * 拦截标注了 @RequireAdmin 注解的方法，验证是否为管理员
 */
@Slf4j
@Aspect

@Component
public class PermissionAspect {

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 拦截所有标注了 @RequireAdmin 注解的方法
     */
    @Around("@annotation(com.example.rpa.annotation.RequireAdmin)")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAdmin requireAdmin = method.getAnnotation(RequireAdmin.class);
        
        String permissionDesc = requireAdmin.value();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        log.info("权限验证 - 方法: {}, 描述: {}", methodName, permissionDesc);
        
        if (!securityUtil.isAdmin()) {
            log.warn("权限拒绝 - 非管理员尝试访问: {}", methodName);
            throw new BusinessException("权限不足：" + permissionDesc);
        }
        
        log.info("权限验证通过 - 方法: {}", methodName);
        return joinPoint.proceed();
    }
}
