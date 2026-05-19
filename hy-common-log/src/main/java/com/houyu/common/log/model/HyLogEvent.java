package com.houyu.common.log.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class HyLogEvent {

    private String eventId;
    private LocalDateTime timestamp;
    private LogLevel level;
    private String loggerName;
    private String threadName;
    private String message;
    private String formattedMessage;

    private String exceptionClassName;
    private String exceptionMessage;
    private String stackTrace;

    private String className;
    private String methodName;
    private String fileName;
    private Integer lineNumber;

    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String serviceName;
    private String serviceVersion;
    private String environment;

    private HttpRequestInfo httpRequest;
    private HttpResponseInfo httpResponse;
    private Long executionTime;
    private Boolean success;
    private Boolean replayable;
    private String requestHash;
    private Integer replayCount;

    private String userId;
    private String username;
    private String tenantId;

    private String serverIp;
    private String clientIp;
    private Map<String, String> mdcContext;

    private Map<String, Object> extensions;
}