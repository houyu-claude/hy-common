package com.houyu.common.log.trace;

public interface TraceIdGenerator {
    String generateTraceId();
    String generateTraceId(String flag);
    String generateSpanId();
    String generateChildSpanId(String parentSpanId);
}