package com.houyu.common.log.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houyu.common.log.config.LogProperties;
import com.houyu.common.log.model.HyLogEvent;
import io.micrometer.core.instrument.Metrics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class AsyncDbLogWriter {

    private static final int QUEUE_CAPACITY = 65536;

    private final BlockingQueue<HyLogEvent> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final JdbcTemplate jdbcTemplate;
    private final LogTableRouter tableRouter;
    private final int batchSize;
    private final ScheduledExecutorService scheduler;
    private final ObjectMapper objectMapper;

    public AsyncDbLogWriter(JdbcTemplate jdbcTemplate, LogTableRouter tableRouter, 
                           LogProperties logProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableRouter = tableRouter;
        this.batchSize = logProperties.getDb().getBatchSize();
        this.objectMapper = new ObjectMapper();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "log-db-writer");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::flush, 
            logProperties.getDb().getFlushIntervalMs(), 
            logProperties.getDb().getFlushIntervalMs(), 
            TimeUnit.MILLISECONDS);
    }

    public void write(HyLogEvent event) {
        if (!queue.offer(event)) {
            Metrics.counter("hy.log.db.dropped").increment();
        }
    }

    private void flush() {
        List<HyLogEvent> batch = new ArrayList<>(batchSize);
        queue.drainTo(batch, batchSize);
        if (batch.isEmpty()) return;

        Map<String, List<HyLogEvent>> byTable = batch.stream()
                .collect(Collectors.groupingBy(
                        e -> tableRouter.resolveTableName("sys_log", e.getTraceId())));

        byTable.forEach((tableName, events) -> {
            try {
                tableRouter.ensureTableExists(tableName);
                batchInsertLog(tableName, events);
                
                String httpRequestTable = tableName.replace("sys_log", "sys_log_http_request");
                String httpResponseTable = tableName.replace("sys_log", "sys_log_http_response");
                tableRouter.ensureTableExists(httpRequestTable);
                tableRouter.ensureTableExists(httpResponseTable);
                
                batchInsertHttpRequest(httpRequestTable, events);
                batchInsertHttpResponse(httpResponseTable, events);
            } catch (Exception ex) {
                Metrics.counter("hy.log.db.failed").increment(events.size());
            }
        });
    }

    private void batchInsertLog(String tableName, List<HyLogEvent> events) {
        String sql = "INSERT INTO " + tableName +
                " (event_id, trace_id, span_id, parent_span_id, service_name, service_version, environment," +
                "  log_level, logger_name, thread_name, class_name, method_name, file_name, line_number," +
                "  message, formatted_message, exception_class_name, exception_message, stack_trace," +
                "  user_id, username, tenant_id, server_ip, client_ip, execution_time, is_success," +
                "  log_timestamp, created_at)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        jdbcTemplate.batchUpdate(sql, events, events.size(), (ps, e) -> {
            ps.setString(1, e.getEventId());
            ps.setString(2, e.getTraceId());
            ps.setString(3, e.getSpanId());
            ps.setString(4, e.getParentSpanId());
            ps.setString(5, e.getServiceName());
            ps.setString(6, e.getServiceVersion());
            ps.setString(7, e.getEnvironment());
            ps.setString(8, e.getLevel() != null ? e.getLevel().name() : null);
            ps.setString(9, e.getLoggerName());
            ps.setString(10, e.getThreadName());
            ps.setString(11, e.getClassName());
            ps.setString(12, e.getMethodName());
            ps.setString(13, e.getFileName());
            ps.setObject(14, e.getLineNumber());
            ps.setString(15, e.getMessage());
            ps.setString(16, e.getFormattedMessage());
            ps.setString(17, e.getExceptionClassName());
            ps.setString(18, e.getExceptionMessage());
            ps.setString(19, e.getStackTrace());
            ps.setString(20, e.getUserId());
            ps.setString(21, e.getUsername());
            ps.setString(22, e.getTenantId());
            ps.setString(23, e.getServerIp());
            ps.setString(24, e.getClientIp());
            ps.setObject(25, e.getExecutionTime());
            ps.setObject(26, e.getSuccess());
            ps.setObject(27, e.getTimestamp());
        });
    }

    private void batchInsertHttpRequest(String tableName, List<HyLogEvent> events) {
        List<HyLogEvent> withRequest = events.stream()
                .filter(e -> e.getHttpRequest() != null)
                .collect(Collectors.toList());
        if (withRequest.isEmpty()) return;

        String sql = "INSERT INTO " + tableName +
                " (event_id, trace_id, method, uri, url, query_string, content_type, user_agent, referer, protocol," +
                "  headers, cookies, request_body, parameters, created_at)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        jdbcTemplate.batchUpdate(sql, withRequest, withRequest.size(), (ps, e) -> {
            var req = e.getHttpRequest();
            ps.setString(1, e.getEventId());
            ps.setString(2, e.getTraceId());
            ps.setString(3, req.getMethod());
            ps.setString(4, req.getUri());
            ps.setString(5, req.getUrl());
            ps.setString(6, req.getQueryString());
            ps.setString(7, req.getContentType());
            ps.setString(8, req.getUserAgent());
            ps.setString(9, req.getReferer());
            ps.setString(10, req.getProtocol());
            ps.setString(11, toJson(req.getHeaders()));
            ps.setString(12, toJson(req.getCookies()));
            ps.setString(13, req.getRequestBody());
            ps.setString(14, toJson(req.getParameters()));
        });
    }

    private void batchInsertHttpResponse(String tableName, List<HyLogEvent> events) {
        List<HyLogEvent> withResponse = events.stream()
                .filter(e -> e.getHttpResponse() != null)
                .collect(Collectors.toList());
        if (withResponse.isEmpty()) return;

        String sql = "INSERT INTO " + tableName +
                " (event_id, trace_id, status_code, content_type, content_length, error_code, error_message," +
                "  headers, response_body, created_at)" +
                " VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        jdbcTemplate.batchUpdate(sql, withResponse, withResponse.size(), (ps, e) -> {
            var resp = e.getHttpResponse();
            ps.setString(1, e.getEventId());
            ps.setString(2, e.getTraceId());
            ps.setObject(3, resp.getStatusCode());
            ps.setString(4, resp.getContentType());
            ps.setObject(5, resp.getContentLength());
            ps.setString(6, resp.getErrorCode());
            ps.setString(7, resp.getErrorMessage());
            ps.setString(8, toJson(resp.getHeaders()));
            ps.setString(9, resp.getResponseBody());
        });
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        flush();
    }
}