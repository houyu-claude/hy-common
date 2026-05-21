package com.houyu.common.app.aop.controller;

import com.houyu.common.app.annotation.AuditLog;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Order(40)
public class AuditAspect {

    private final LogOutputManager logOutputManager;

    public AuditAspect(LogOutputManager logOutputManager) {
        this.logOutputManager = logOutputManager;
    }

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Exception exception = null;
        boolean success = true;

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            exception = e;
            success = false;
            throw e;
        } finally {
            HyLogEvent logEvent = new HyLogEvent();
            logEvent.setTraceId(RequestContextHolder.getTraceId());
            logEvent.setTimestamp(LocalDateTime.now());
            logEvent.setLevel(success ?
                    com.houyu.common.log.model.LogLevel.INFO :
                    com.houyu.common.log.model.LogLevel.ERROR);
            logEvent.setMessage("Audit log: " + auditLog.description() +
                    (success ? " - success" : " - failed"));
            logEvent.setServiceName("hy-common-app");
            logEvent.setMethodName(joinPoint.getSignature().getName());
            logEvent.setClassName(joinPoint.getTarget().getClass().getName());
            logEvent.setUserId(RequestContextHolder.getUserId());
            logEvent.setSuccess(success);

            if (!success && exception != null) {
                logEvent.setExceptionClassName(exception.getClass().getName());
                logEvent.setExceptionMessage(exception.getMessage());
            }

            logOutputManager.output(logEvent);
        }
    }
}