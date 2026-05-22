package com.houyu.common.app.aop.controller;

import com.houyu.common.app.context.RequestContextHolder;
import com.houyu.common.app.exception.AccessDeniedException;
import com.houyu.common.app.exception.BusinessException;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATE_ERROR_MESSAGE = "操作冲突，请稍后重试";

    private final LogOutputManager logOutputManager;
    private final String serviceName;

    public GlobalExceptionHandler(LogOutputManager logOutputManager,
                                 @Value("${spring.application.name:hy-common-app}") String serviceName) {
        this.logOutputManager = logOutputManager;
        this.serviceName = serviceName;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        BindingResult bindingResult = ex.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        logException(ex, HttpStatus.BAD_REQUEST.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "VALIDATION_ERROR");
        response.put("message", "参数校验失败");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex) {

        logException(ex, HttpStatus.CONFLICT.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "STATE_ERROR");
        response.put("message", STATE_ERROR_MESSAGE);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        logException(ex, HttpStatus.BAD_REQUEST.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "INVALID_ARGUMENT");
        response.put("message", "请求参数无效");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {

        logException(ex, HttpStatus.BAD_REQUEST.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", ex.getCode());
        response.put("message", ex.getMessage());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {

        logException(ex, HttpStatus.FORBIDDEN.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", ex.getCode());
        response.put("message", "权限不足，无法访问");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {

        logException(ex, HttpStatus.FORBIDDEN.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "ACCESS_DENIED");
        response.put("message", "权限不足，无法访问");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {

        logException(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("code", "INTERNAL_ERROR");
        response.put("message", "系统内部错误，请联系管理员");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("traceId", RequestContextHolder.getTraceId());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private void logException(Exception ex, int statusCode) {
        HyLogEvent logEvent = new HyLogEvent();
        logEvent.setTraceId(RequestContextHolder.getTraceId());
        logEvent.setTimestamp(LocalDateTime.now());
        logEvent.setLevel(com.houyu.common.log.model.LogLevel.ERROR);
        logEvent.setMessage("Global exception: " + ex.getClass().getSimpleName());
        logEvent.setServiceName(serviceName);
        logEvent.setMethodName("GlobalExceptionHandler");
        logEvent.setExceptionClassName(ex.getClass().getName());
        logEvent.setExceptionMessage(ex.getMessage());
        logEvent.setSuccess(false);

        if (statusCode >= 500) {
            logEvent.setExceptionStackTrace(getStackTrace(ex));
        }

        logOutputManager.output(logEvent);
    }

    private String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 2000) {
                sb.append("\t... (truncated)");
                break;
            }
        }
        return sb.toString();
    }
}