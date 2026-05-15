package com.houyu.common.log.converter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.model.LogLevel;
import com.houyu.common.log.trace.TraceContextHolder;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class LogEventConverter {

    public HyLogEvent convert(ILoggingEvent event) {
        HyLogEvent hyLogEvent = new HyLogEvent();

        hyLogEvent.setTimestamp(LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault()));
        hyLogEvent.setLevel(convertLevel(event.getLevel()));
        hyLogEvent.setLoggerName(event.getLoggerName());
        hyLogEvent.setThreadName(event.getThreadName());
        hyLogEvent.setMessage(event.getMessage());
        hyLogEvent.setFormattedMessage(event.getFormattedMessage());

        fillExceptionInfo(hyLogEvent, event);
        fillCallerInfo(hyLogEvent, event);
        fillTraceInfo(hyLogEvent, event);
        fillMdcContext(hyLogEvent, event);

        return hyLogEvent;
    }

    private LogLevel convertLevel(ch.qos.logback.classic.Level level) {
        if (level == null) return LogLevel.INFO;
        return switch (level.toInt()) {
            case ch.qos.logback.classic.Level.TRACE_INT -> LogLevel.TRACE;
            case ch.qos.logback.classic.Level.DEBUG_INT -> LogLevel.DEBUG;
            case ch.qos.logback.classic.Level.INFO_INT -> LogLevel.INFO;
            case ch.qos.logback.classic.Level.WARN_INT -> LogLevel.WARN;
            case ch.qos.logback.classic.Level.ERROR_INT -> LogLevel.ERROR;
            default -> LogLevel.INFO;
        };
    }

    private void fillExceptionInfo(HyLogEvent hyLogEvent, ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            hyLogEvent.setExceptionClassName(throwableProxy.getClassName());
            hyLogEvent.setExceptionMessage(throwableProxy.getMessage());
            hyLogEvent.setStackTrace(convertStackTrace(throwableProxy));
        }
    }

    private String convertStackTrace(IThrowableProxy throwableProxy) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(throwableProxy.getClassName() + ": " + throwableProxy.getMessage());
        StackTraceElementProxy[] stackTraceElements = throwableProxy.getStackTraceElementProxyArray();
        for (StackTraceElementProxy element : stackTraceElements) {
            pw.println("\tat " + element.getStackTraceElement().toString());
        }
        return sw.toString();
    }

    private void fillCallerInfo(HyLogEvent hyLogEvent, ILoggingEvent event) {
        StackTraceElement[] callerData = event.getCallerData();
        if (callerData != null && callerData.length > 0) {
            StackTraceElement caller = callerData[0];
            hyLogEvent.setClassName(caller.getClassName());
            hyLogEvent.setMethodName(caller.getMethodName());
            hyLogEvent.setFileName(caller.getFileName());
            hyLogEvent.setLineNumber(caller.getLineNumber());
        }
    }

    private void fillTraceInfo(HyLogEvent hyLogEvent, ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null) {
            hyLogEvent.setTraceId(mdc.getOrDefault("traceId", TraceContextHolder.getTraceId()));
            hyLogEvent.setSpanId(mdc.getOrDefault("spanId", TraceContextHolder.getSpanId()));
            hyLogEvent.setParentSpanId(mdc.get("parentSpanId"));
        } else {
            hyLogEvent.setTraceId(TraceContextHolder.getTraceId());
            hyLogEvent.setSpanId(TraceContextHolder.getSpanId());
        }
    }

    private void fillMdcContext(HyLogEvent hyLogEvent, ILoggingEvent event) {
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc != null) {
            hyLogEvent.setMdcContext(new HashMap<>(mdc));
        }
    }
}