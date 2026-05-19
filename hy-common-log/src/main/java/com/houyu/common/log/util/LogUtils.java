package com.houyu.common.log.util;

import com.houyu.common.log.model.HyLogEvent;
import org.slf4j.MDC;

public class LogUtils {

    private LogUtils() {}

    public static void setTraceId(String traceId) {
        MDC.put("traceId", traceId);
    }

    public static void setSpanId(String spanId) {
        MDC.put("spanId", spanId);
    }

    public static void setParentSpanId(String parentSpanId) {
        MDC.put("parentSpanId", parentSpanId);
    }

    public static void setTraceFlag(String traceFlag) {
        MDC.put("traceFlag", traceFlag);
    }

    public static String getTraceId() {
        return MDC.get("traceId");
    }

    public static String getSpanId() {
        return MDC.get("spanId");
    }

    public static void clearMdc() {
        MDC.clear();
    }

    public static boolean isValidTraceId(String traceId) {
        if (traceId == null || traceId.length() != 19) {
            return false;
        }
        return traceId.matches("\\d{19}");
    }

    public static String extractDateFromTraceId(String traceId) {
        if (traceId == null || traceId.length() < 6) {
            return null;
        }
        return "20" + traceId.substring(0, 6);
    }
}