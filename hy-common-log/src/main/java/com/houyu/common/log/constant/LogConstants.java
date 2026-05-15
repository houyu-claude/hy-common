package com.houyu.common.log.constant;

public class LogConstants {

    private LogConstants() {}

    public static final String APPENDER_NAME = "HY_COMMON_LOG";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String SPAN_ID_HEADER = "X-Span-Id";
    public static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span-Id";
    public static final String TRACE_FLAG_HEADER = "X-Trace-Flag";

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_PARENT_SPAN_ID = "parentSpanId";
    public static final String MDC_TRACE_FLAG = "traceFlag";

    public static final String METRIC_LOG_DROPPED = "hy.log.dropped";
    public static final String METRIC_LOG_DB_DROPPED = "hy.log.db.dropped";
    public static final String METRIC_LOG_DB_FAILED = "hy.log.db.failed";

    public static final String DEFAULT_LOG_FORMAT = "json";
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int DEFAULT_DB_BATCH_SIZE = 100;
    public static final long DEFAULT_DB_FLUSH_INTERVAL_MS = 5000;

    public static final String SYS_LOG_TABLE_PREFIX = "sys_log";
    public static final int LOG_RETENTION_DAYS = 30;
}