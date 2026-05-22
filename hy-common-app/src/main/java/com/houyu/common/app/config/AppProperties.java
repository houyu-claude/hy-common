package com.houyu.common.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hy.app")
public class AppProperties {

    private boolean enabled = true;

    private IdempotentConfig idempotent = new IdempotentConfig();
    private PageConfig page = new PageConfig();
    private SecurityConfig security = new SecurityConfig();
    private TraceConfig trace = new TraceConfig();
    private AopConfig aop = new AopConfig();

    @Data
    public static class IdempotentConfig {
        private int processingExpireSeconds = 30;
        private int successExpireSeconds = 300;
    }

    @Data
    public static class PageConfig {
        private int maxSize = 5000;
    }

    @Data
    public static class SecurityConfig {
        private boolean enabled = true;
    }

    @Data
    public static class TraceConfig {
        private boolean required = true;
        private boolean autoGenerate = true;
    }

    @Data
    public static class AopConfig {
        private String controllerPackage = "com.houyu.*.controller";
        private String servicePackage = "com.houyu.*.service";
        private String managerPackage = "com.houyu.*.manager";
    }
}