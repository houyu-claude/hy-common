package com.houyu.common.app.aop.service;

import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
public class ServiceLogAspect {

    private final LogOutputManager logOutputManager;

    public ServiceLogAspect(LogOutputManager logOutputManager) {
        this.logOutputManager = logOutputManager;
    }

    @Around("execution(* com.houyu.*.service..*.*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String traceId = RequestContextHolder.getTraceId();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        Object[] args = joinPoint.getArgs();

        Object result = null;
        Exception exception = null;
        boolean success = true;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            success = false;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            HyLogEvent logEvent = new HyLogEvent();
            logEvent.setTraceId(traceId);
            logEvent.setTimestamp(LocalDateTime.now());
            logEvent.setLevel(success ?
                    com.houyu.common.log.model.LogLevel.DEBUG :
                    com.houyu.common.log.model.LogLevel.ERROR);
            logEvent.setMessage(success ? "Service method success" : "Service method failed");
            logEvent.setSuccess(success);
            logEvent.setExecutionTime(executionTime);
            logEvent.setServiceName("hy-common-app");
            logEvent.setMethodName(methodName);
            logEvent.setClassName(className);

            if (args != null && args.length > 0) {
                logEvent.setMessage("Args: " + Arrays.toString(args));
            }

            if (result != null) {
                logEvent.setMessage(logEvent.getMessage() + ", Result: " + result.toString());
            }

            if (exception != null) {
                logEvent.setExceptionClassName(exception.getClass().getName());
                logEvent.setExceptionMessage(exception.getMessage());
            }

            logOutputManager.output(logEvent);
        }
    }
}