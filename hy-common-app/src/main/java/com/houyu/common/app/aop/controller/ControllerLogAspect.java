package com.houyu.common.app.aop.controller;

import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.model.HttpRequestInfo;
import com.houyu.common.log.model.HttpResponseInfo;
import com.houyu.common.log.output.LogOutputManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
@Order(30)
public class ControllerLogAspect {

    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(Arrays.asList(
            "authorization", "authorization-bearer", "x-api-key", "cookie", "set-cookie",
            "x-user-token", "x-access-token", "x-refresh-token", "password"
    ));

    private final LogOutputManager logOutputManager;
    private final String serviceName;

    public ControllerLogAspect(LogOutputManager logOutputManager, 
                              @Value("${spring.application.name:hy-common-app}") String serviceName) {
        this.logOutputManager = logOutputManager;
        this.serviceName = serviceName;
    }

    @Around("@within(com.houyu.common.app.annotation.EnableAppLogging) && execution(* *..controller..*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        ServletRequestAttributes attributes = (ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        HttpServletResponse response = attributes != null ? attributes.getResponse() : null;

        String traceId = RequestContextHolder.getTraceId();
        String method = request != null ? request.getMethod() : "";
        String url = request != null ? request.getRequestURI() : "";
        String queryString = request != null ? request.getQueryString() : "";

        Map<String, String> requestHeaders = new HashMap<>();
        if (request != null) {
            request.getHeaderNames().asIterator().forEachRemaining(name -> {
                String value = name != null && SENSITIVE_HEADERS.contains(name.toLowerCase())
                        ? "[REDACTED]"
                        : request.getHeader(name);
                requestHeaders.put(name, value);
            });
        }

        String requestBody = getRequestBody(request);

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
            logEvent.setMessage(success ? "Controller request success" : "Controller request failed");
            logEvent.setSuccess(success);
            logEvent.setExecutionTime(executionTime);
            logEvent.setServiceName(serviceName);
            logEvent.setMethodName(joinPoint.getSignature().getName());
            logEvent.setClassName(joinPoint.getTarget().getClass().getName());

            HttpRequestInfo httpRequest = new HttpRequestInfo();
            httpRequest.setMethod(method);
            httpRequest.setUri(url);
            httpRequest.setQueryString(queryString);
            httpRequest.setHeaders(requestHeaders);
            httpRequest.setRequestBody(requestBody);
            logEvent.setHttpRequest(httpRequest);

            if (exception != null) {
                logEvent.setExceptionClassName(exception.getClass().getName());
                logEvent.setExceptionMessage(exception.getMessage());
            }

            if (result != null || response != null) {
                HttpResponseInfo httpResponse = new HttpResponseInfo();
                if (result != null) {
                    httpResponse.setResponseBody(result.toString());
                }

                Map<String, String> responseHeaders = new HashMap<>();
                if (response != null) {
                    response.getHeaderNames().forEach(name -> {
                        responseHeaders.put(name, response.getHeader(name));
                    });
                    httpResponse.setHeaders(responseHeaders);
                    httpResponse.setStatusCode(response.getStatus());
                }
                logEvent.setHttpResponse(httpResponse);
            }

            logOutputManager.output(logEvent);
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String contentType = request.getContentType();
        if (contentType != null && (contentType.contains("application/json") ||
                contentType.contains("application/x-www-form-urlencoded"))) {
            if (request instanceof ContentCachingRequestWrapper wrapper) {
                byte[] body = wrapper.getContentAsByteArray();
                if (body != null && body.length > 0) {
                    return new String(body, StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}