package com.houyu.common.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hy.app")
public class AppProperties {

    private boolean enabled = true;

    private IdempotentConfig idempotent = new IdempotentConfig();
    private PageConfig page = new PageConfig();
    private SecurityConfig security = new SecurityConfig();

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
}