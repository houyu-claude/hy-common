package com.houyu.common.app.aop.service;

import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.app.entity.BaseEntity;
import com.houyu.common.app.enums.OpType;
import com.houyu.common.app.service.JournalService;
import com.houyu.common.app.service.JournalSupport;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class JournalAspect {

    private static final Logger logger = LoggerFactory.getLogger(JournalAspect.class);

    private final JournalService journalService;

    public JournalAspect(JournalService journalService) {
        this.journalService = journalService;
    }

    @Around("execution(* com.houyu.*.service..*.save*(..))")
    public Object recordSaveJournal(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BaseEntity entity) {
                journalService.sendJournal(entity);
            }
        }

        return result;
    }

    @Around("execution(* com.houyu.*.service..*.update*(..))")
    public Object recordUpdateJournal(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BaseEntity entity) {
                journalService.sendJournal(entity);
            }
        }

        return result;
    }

    @Around("execution(* com.houyu.*.service..*.removeById(..))")
    public Object recordDeleteByIdJournal(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object id = args[0];

        BaseEntity entity = findEntityBeforeDelete(joinPoint, id);

        Object result = joinPoint.proceed();

        if (entity != null) {
            entity.setOpType(OpType.DELETE.name());
            journalService.sendJournal(entity);
        }

        return result;
    }

    @Around("execution(* com.houyu.*.service..*.remove(..))")
    public Object recordDeleteJournal(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        BaseEntity entity = extractEntityFromArgs(args);

        if (entity == null) {
            entity = findEntityBeforeDelete(joinPoint, args);
        }

        Object result = joinPoint.proceed();

        if (entity != null) {
            entity.setOpType(OpType.DELETE.name());
            journalService.sendJournal(entity);
        }

        return result;
    }

    private BaseEntity extractEntityFromArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof BaseEntity) {
                return (BaseEntity) arg;
            }
        }
        return null;
    }

    private BaseEntity findEntityBeforeDelete(ProceedingJoinPoint joinPoint, Object... args) {
        Object target = joinPoint.getTarget();
        if (target instanceof JournalSupport journalSupport) {
            if (args.length > 0 && args[0] != null) {
                return journalSupport.getEntityBeforeDelete(args[0]);
            }
        }
        logger.debug("Target service does not implement JournalSupport interface: {}", 
                joinPoint.getTarget().getClass().getName());
        return null;
    }
}