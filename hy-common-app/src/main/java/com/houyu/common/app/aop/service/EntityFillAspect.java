package com.houyu.common.app.aop.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.app.entity.BaseEntity;
import com.houyu.common.app.enums.OpType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;

@Aspect
@Component
@Order(1)
public class EntityFillAspect {

    @Before("execution(* com.houyu.*.service..*.save*(..)) && !execution(* com.houyu.*.service..*.saveOrUpdate*(..))")
    public void fillForSave(JoinPoint joinPoint) {
        fillEntity(joinPoint, OpType.INSERT);
    }

    @Before("execution(* com.houyu.*.service..*.update*(..))")
    public void fillForUpdate(JoinPoint joinPoint) {
        fillEntity(joinPoint, OpType.UPDATE);
    }

    @Before("execution(* com.houyu.*.service..*.remove*(..))")
    public void fillForDelete(JoinPoint joinPoint) {
        fillEntity(joinPoint, OpType.DELETE);
    }

    @Before("execution(* com.houyu.*.service..*.saveOrUpdate*(..))")
    public void fillForSaveOrUpdate(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BaseEntity entity) {
                OpType opType = entity.getId() == null ? OpType.INSERT : OpType.UPDATE;
                fillEntity(joinPoint, opType);
                return;
            }
        }
        fillEntity(joinPoint, OpType.INSERT);
    }

    private void fillEntity(JoinPoint joinPoint, OpType opType) {
        String traceId = RequestContextHolder.getTraceId();
        String userName = RequestContextHolder.getUserName();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BaseEntity entity) {
                fillBaseEntity(entity, traceId, userName, opType);
            } else if (arg instanceof Collection<?> collection) {
                collection.forEach(item -> {
                    if (item instanceof BaseEntity entity) {
                        fillBaseEntity(entity, traceId, userName, opType);
                    }
                });
            } else if (arg instanceof Wrapper<?> wrapper) {
                Object entity = extractEntityFromWrapper(wrapper);
                if (entity instanceof BaseEntity baseEntity) {
                    fillBaseEntity(baseEntity, traceId, userName, opType);
                }
            }
        }
    }

    private void fillBaseEntity(BaseEntity entity, String traceId, String userName, OpType opType) {
        entity.setTraceId(traceId);
        entity.setOpType(opType.name());
        entity.setOwner(userName);

        if (opType == OpType.INSERT) {
            entity.setCreater(userName);
            entity.setCreateTime(LocalDateTime.now());
        }

        entity.setUpdater(userName);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setIsCurrent(true);
    }

    private Object extractEntityFromWrapper(Object wrapper) {
        if (wrapper instanceof AbstractWrapper abstractWrapper) {
            return abstractWrapper.getEntity();
        }
        return null;
    }
}