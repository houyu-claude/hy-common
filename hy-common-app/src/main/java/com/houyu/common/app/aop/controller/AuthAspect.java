package com.houyu.common.app.aop.controller;

import com.houyu.common.app.annotation.RequirePermission;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.app.service.PermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(20)
public class AuthAspect {

    private final PermissionService permissionService;

    public AuthAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String userId = RequestContextHolder.getUserId();
        String permission = requirePermission.value();

        if (!permissionService.hasPermission(userId, permission)) {
            throw new SecurityException("Insufficient permission: " + permission);
        }

        String dataScope = permissionService.getDataScope(userId);
        RequestContextHolder.setDataScopeSql(dataScope);

        return joinPoint.proceed();
    }
}