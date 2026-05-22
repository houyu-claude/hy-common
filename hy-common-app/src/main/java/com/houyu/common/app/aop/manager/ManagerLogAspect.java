package com.houyu.common.app.aop.manager;

import com.houyu.common.app.annotation.Sensitive;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Aspect
@Component
public class ManagerLogAspect {

    private final LogOutputManager logOutputManager;
    private final String serviceName;

    public ManagerLogAspect(LogOutputManager logOutputManager,
                           @Value("${spring.application.name:hy-common-app}") String serviceName) {
        this.logOutputManager = logOutputManager;
        this.serviceName = serviceName;
    }

    @Around("@within(com.houyu.common.app.annotation.EnableAppLogging) && execution(* *..manager..*.*(..))")
    public Object logManager(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String traceId = RequestContextHolder.getTraceId();
        String methodSignature = joinPoint.getSignature().toShortString();
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
                    com.houyu.common.log.model.LogLevel.INFO :
                    com.houyu.common.log.model.LogLevel.ERROR);
            logEvent.setMessage(success ? "Manager method success" : "Manager method failed");
            logEvent.setSuccess(success);
            logEvent.setExecutionTime(executionTime);
            logEvent.setServiceName(serviceName);
            logEvent.setMethodName(methodSignature);
            logEvent.setClassName(className);
            logEvent.setMdcContext(new HashMap<>(RequestContextHolder.getMdcContext()));

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