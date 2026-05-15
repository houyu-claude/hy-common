package com.houyu.common.log.trace;

public class TraceContextHolder {

    private static final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> spanIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> parentSpanIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> traceFlagHolder = new ThreadLocal<>();

    public static String getTraceId() {
        return traceIdHolder.get();
    }

    public static void setTraceId(String traceId) {
        traceIdHolder.set(traceId);
    }

    public static String getSpanId() {
        return spanIdHolder.get();
    }

    public static void setSpanId(String spanId) {
        spanIdHolder.set(spanId);
    }

    public static String getParentSpanId() {
        return parentSpanIdHolder.get();
    }

    public static void setParentSpanId(String parentSpanId) {
        parentSpanIdHolder.set(parentSpanId);
    }

    public static String getTraceFlag() {
        return traceFlagHolder.get();
    }

    public static void setTraceFlag(String traceFlag) {
        traceFlagHolder.set(traceFlag);
    }

    public static void clear() {
        traceIdHolder.remove();
        spanIdHolder.remove();
        parentSpanIdHolder.remove();
        traceFlagHolder.remove();
    }
}