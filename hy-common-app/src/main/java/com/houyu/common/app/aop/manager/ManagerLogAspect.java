package com.houyu.common.app.aop.manager;

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
public class ManagerLogAspect {

    private final LogOutputManager logOutputManager;

    public ManagerLogAspect(LogOutputManager logOutputManager) {
        this.logOutputManager = logOutputManager;
    }

    @Around("execution(* com.houyu.*.manager..*.*(..))")
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
            logEvent.setServiceName("hy-common-app");
            logEvent.setMethodName(methodSignature);
            logEvent.setClassName(className);
            logEvent.setMdcContext(new java.util.HashMap<>(RequestContextHolder.getMdcContext()));

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