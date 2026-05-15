package com.houyu.common.log.output;

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

    public AsyncDbLogWriter(JdbcTemplate jdbcTemplate, LogTableRouter tableRouter) {
        this(jdbcTemplate, tableRouter, 100, 5000);
    }

    public AsyncDbLogWriter(JdbcTemplate jdbcTemplate, LogTableRouter tableRouter,
                            int batchSize, long flushIntervalMs) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableRouter = tableRouter;
        this.batchSize = batchSize;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "log-db-writer");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::flush, flushIntervalMs, flushIntervalMs, TimeUnit.MILLISECONDS);
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
                batchInsert(tableName, events);
            } catch (Exception ex) {
                Metrics.counter("hy.log.db.failed").increment(events.size());
            }
        });
    }

    private void batchInsert(String tableName, List<HyLogEvent> events) {
        String sql = "INSERT INTO " + tableName +
                " (event_id, trace_id, span_id, parent_span_id, service_name, log_level," +
                "  message, user_id, client_ip, server_ip, execution_time, is_success," +
                "  log_timestamp, created_at)" +
                " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        jdbcTemplate.batchUpdate(sql, events, events.size(), (ps, e) -> {
            ps.setString(1, e.getEventId());
            ps.setString(2, e.getTraceId());
            ps.setString(3, e.getSpanId());
            ps.setString(4, e.getParentSpanId());
            ps.setString(5, e.getServiceName());
            ps.setString(6, e.getLevel() != null ? e.getLevel().name() : null);
            ps.setString(7, e.getMessage());
            ps.setString(8, e.getUserId());
            ps.setString(9, e.getClientIp());
            ps.setString(10, e.getServerIp());
            ps.setObject(11, e.getExecutionTime());
            ps.setObject(12, e.getSuccess());
            ps.setObject(13, e.getTimestamp());
        });
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        flush();
    }
}