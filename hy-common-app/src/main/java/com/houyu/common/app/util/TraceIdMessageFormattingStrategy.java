package com.houyu.common.app.util;

import com.houyu.common.app.context.RequestContextHolder;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

public class TraceIdMessageFormattingStrategy implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        String traceId = RequestContextHolder.getTraceId();
        if (traceId == null) {
            traceId = "N/A";
        }
        return String.format("%s | %dms | %s | traceId=%s | connection%d | %s",
                now, elapsed, category, traceId, connectionId, sql);
    }
}