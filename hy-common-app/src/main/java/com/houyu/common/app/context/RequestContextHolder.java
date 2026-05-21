package com.houyu.common.app.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

public class RequestContextHolder {

    private static final TransmittableThreadLocal<String> traceIdHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> requestIdHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> userIdHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> userNameHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<Boolean> retryFlagHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<String> dataScopeSqlHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<Map<String, String>> requestHeadersHolder = new TransmittableThreadLocal<>();
    private static final TransmittableThreadLocal<Map<String, String>> mdcContextHolder = new TransmittableThreadLocal<>();

    public static void setTraceId(String traceId) {
        traceIdHolder.set(traceId);
        updateMdc("traceId", traceId);
    }

    public static String getTraceId() {
        return traceIdHolder.get();
    }

    public static void setRequestId(String requestId) {
        requestIdHolder.set(requestId);
        updateMdc("requestId", requestId);
    }

    public static String getRequestId() {
        return requestIdHolder.get();
    }

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
        updateMdc("userId", userId);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static void setUserName(String userName) {
        userNameHolder.set(userName);
        updateMdc("userName", userName);
    }

    public static String getUserName() {
        return userNameHolder.get();
    }

    public static void setRetryFlag(Boolean retryFlag) {
        retryFlagHolder.set(retryFlag);
    }

    public static Boolean getRetryFlag() {
        return retryFlagHolder.get();
    }

    public static void setDataScopeSql(String dataScopeSql) {
        dataScopeSqlHolder.set(dataScopeSql);
    }

    public static String getDataScopeSql() {
        return dataScopeSqlHolder.get();
    }

    public static void setRequestHeaders(Map<String, String> headers) {
        requestHeadersHolder.set(headers);
    }

    public static Map<String, String> getRequestHeaders() {
        return requestHeadersHolder.get();
    }

    public static Map<String, String> getMdcContext() {
        return mdcContextHolder.get() != null ? mdcContextHolder.get() : new HashMap<>();
    }

    private static void updateMdc(String key, String value) {
        Map<String, String> mdc = mdcContextHolder.get();
        if (mdc == null) {
            mdc = new HashMap<>();
            mdcContextHolder.set(mdc);
        }
        mdc.put(key, value);
    }

    public static void clear() {
        traceIdHolder.remove();
        requestIdHolder.remove();
        userIdHolder.remove();
        userNameHolder.remove();
        retryFlagHolder.remove();
        dataScopeSqlHolder.remove();
        requestHeadersHolder.remove();
        mdcContextHolder.remove();
    }
}