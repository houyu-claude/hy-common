package com.houyu.common.app.aop.service;

import com.houyu.common.app.annotation.Sensitive;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                String argsInfo = buildArgsInfo(joinPoint);
                logEvent.setMessage(logEvent.getMessage() + ", Args: " + argsInfo);
            }

            if (result != null) {
                logEvent.setMessage(logEvent.getMessage() + ", Result type: " + result.getClass().getSimpleName());
            }

            if (exception != null) {
                logEvent.setExceptionClassName(exception.getClass().getName());
                logEvent.setExceptionMessage(exception.getMessage());
            }

            logOutputManager.output(logEvent);
        }
    }

    private String buildArgsInfo(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] paramTypes = signature.getParameterTypes();
        java.lang.reflect.Parameter[] parameters = signature.getMethod().getParameters();

        List<String> argDescriptions = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String paramName = parameters[i].getName();
            Class<?> paramType = paramTypes[i];
            boolean isSensitive = parameters[i].isAnnotationPresent(Sensitive.class);

            String argInfo = paramName + ": " + paramType.getSimpleName();
            if (isSensitive) {
                argInfo += " [SENSITIVE]";
            }
            argDescriptions.add(argInfo);
        }

        return "[" + String.join(", ", argDescriptions) + "]";
    }
}