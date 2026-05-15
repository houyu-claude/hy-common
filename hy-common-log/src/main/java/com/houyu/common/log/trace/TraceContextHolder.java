package com.houyu.common.log.trace;

import com.alibaba.ttl.TransmittableThreadLocal;

public class TraceContextHolder {

    private static final TransmittableThreadLocal<String> traceIdHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> spanIdHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> parentSpanIdHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> traceFlagHolder = new TransmittableThreadLocal<>();

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