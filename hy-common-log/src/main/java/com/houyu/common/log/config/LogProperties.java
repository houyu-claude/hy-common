package com.houyu.common.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hy.log")
public class LogProperties {

    private boolean enabled = true;
    private String format = "json";

    private AppenderConfig appender = new AppenderConfig();
    private TraceConfig trace = new TraceConfig();
    private ConsoleOutputConfig console = new ConsoleOutputConfig();
    private FileOutputConfig file = new FileOutputConfig();
    private DbOutputConfig db = new DbOutputConfig();
    private DesensitizeConfig desensitize = new DesensitizeConfig();
    private FilterConfig filter = new FilterConfig();
    private SamplerConfig sampler = new SamplerConfig();
    private ReplayConfig replay = new ReplayConfig();

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
    public static class ConsoleOutputConfig {
        private boolean enabled = true;
        private String pattern = "[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [traceId=%X{traceId}] [%thread] %logger{36} - %msg%n";
    }

    @Data
    public static class FileOutputConfig {
        private boolean enabled = true;
        private String pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] [%thread] %logger{36} - %msg%n";
        private String path = "./logs";
        private String name = "hy-common.log";
        private int maxFileSize = 100;
        private int maxHistory = 30;
        private int retentionDays = 30;
    }

    @Data
    public static class DbOutputConfig {
        private boolean enabled = false;
        private int batchSize = 100;
        private long flushIntervalMs = 5000;
        private int retentionDays = 30;
    }

    @Data
    public static class DesensitizeConfig {
        private boolean enabled = true;
    }

    @Data
    public static class FilterConfig {
        private boolean enabled = true;
        private String minLevel = "TRACE";
        private java.util.List<String> keywords = java.util.Collections.emptyList();
    }

    @Data
    public static class SamplerConfig {
        private boolean enabled = false;
        private double rate = 1.0;
    }

    @Data
    public static class ReplayConfig {
        private boolean enabled = false;
        private int maxReplayCount = 3;
        private long expireMinutes = 30;
    }
}