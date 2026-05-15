package com.houyu.common.log.output;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LogTableRouter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final Set<String> existingTables = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final JdbcTemplate jdbcTemplate;

    public LogTableRouter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String resolveTableName(String baseTable, String traceId) {
        if (traceId != null && traceId.length() >= 6) {
            String yyMMdd = traceId.substring(0, 6);
            return baseTable + "_20" + yyMMdd;
        }
        return baseTable + "_" + LocalDate.now().format(DATE_FMT);
    }

    public void ensureTableExists(String tableName) {
        if (existingTables.contains(tableName)) return;
        String baseTable = tableName.replaceAll("_\\d{8}$", "");
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS " + tableName +
                " (LIKE " + baseTable + " INCLUDING ALL)"
        );
        existingTables.add(tableName);
    }

    @Scheduled(cron = "0 55 23 * * *")
    public void preCreateNextDayTables() {
        String nextDay = LocalDate.now().plusDays(1).format(DATE_FMT);
        ensureTableExists("sys_log_" + nextDay);
        ensureTableExists("sys_log_http_request_" + nextDay);
        ensureTableExists("sys_log_http_response_" + nextDay);
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanOldTables() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        String cutoffStr = "20" + cutoff.format(DateTimeFormatter.ofPattern("yyMMdd"));
        jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = current_schema() AND table_name ~ '^sys_log_\\d{8}$'",
                String.class
        ).stream()
         .filter(name -> {
             String datePart = name.replaceAll("^sys_log_", "");
             return datePart.compareTo(cutoffStr) < 0;
         })
         .forEach(name -> jdbcTemplate.execute("DROP TABLE IF EXISTS " + name));
    }
}