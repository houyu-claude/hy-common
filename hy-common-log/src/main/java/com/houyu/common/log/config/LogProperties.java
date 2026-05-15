package com.houyu.common.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hy.log")
public class LogProperties {

    private boolean enabled = true;

    private AppenderConfig appender = new AppenderConfig();

    private TraceConfig trace = new TraceConfig();

    private OutputConfig output = new OutputConfig();

    private DesensitizeConfig desensitize = new DesensitizeConfig();

    @Data
    public static class AppenderConfig {
        private int bufferSize = 8192;
        private String name = "HY_COMMON_LOG";
    }

    @Data
    public static class TraceConfig {
        private boolean enabled = true;
        private String redisAddress = "redis://localhost:6379";
        private int redisDatabase = 0;
    }

    @Data
    public static class OutputConfig {
        private String format = "json";
        private String consolePattern = "[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [traceId=%X{traceId}] [%thread] %logger{36} - %msg%n";
        private String filePattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] [%thread] %logger{36} - %msg%n";
        private String filePath = "./logs";
        private String fileName = "hy-common.log";
        private int maxFileSize = 100;
        private int maxHistory = 30;
        private boolean dbEnabled = false;
        private int dbBatchSize = 100;
        private long dbFlushIntervalMs = 5000;
    }

    @Data
    public static class DesensitizeConfig {
        private boolean enabled = true;
    }
}