package com.houyu.common.app.aop.controller;

import com.houyu.common.app.annotation.Idempotent;
import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.app.enums.IdempotentStatus;
import com.houyu.common.app.exception.BusinessException;
import com.houyu.common.app.service.IdempotentService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Order(10)
public class IdempotentAspect {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private final IdempotentService idempotentService;

    public IdempotentAspect(IdempotentService idempotentService) {
        this.idempotentService = idempotentService;
    }

    @Around("@annotation(idempotent)")
    public Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = getRequest();
        if (request == null) {
            throw new BusinessException("REQUEST_CONTEXT_ERROR", "HttpServletRequest is null, cannot perform idempotent check");
        }

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        boolean retryFlag = Boolean.parseBoolean(request.getHeader("X-Retry-Flag"));

        if (requestId == null || requestId.isEmpty()) {
            throw new BusinessException("REQUEST_ID_REQUIRED", "RequestId is required for idempotent check");
        }

        String key = buildIdempotentKey(requestId, request.getMethod(), request.getRequestURI());

        boolean acquired = idempotentService.tryStoreProcessing(key);
        if (!acquired) {
            IdempotentStatus status = idempotentService.getStatus(key);
            if (status == IdempotentStatus.PROCESSING) {
                throw new BusinessException("REQUEST_PROCESSING", "Request is processing");
            }
            if (!retryFlag && status == IdempotentStatus.SUCCESS) {
                throw new BusinessException("DUPLICATE_REQUEST", "Duplicate request detected");
            }
        }

        try {
            Object result = joinPoint.proceed();
            idempotentService.storeSuccess(key);
            return result;
        } catch (Exception e) {
            idempotentService.remove(key);
            throw e;
        }
    }

    private String buildIdempotentKey(String requestId, String method, String uri) {
        return "idempotent:" + method + ":" + uri + ":" + requestId;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}