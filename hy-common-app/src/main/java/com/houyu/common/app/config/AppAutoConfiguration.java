package com.houyu.common.app.config;

import com.houyu.common.app.service.DefaultPermissionService;
import com.houyu.common.app.service.IdempotentService;
import com.houyu.common.app.service.JournalService;
import com.houyu.common.app.service.PermissionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "hy.app.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AppProperties.class)
public class AppAutoConfiguration {

    @Bean
    public IdempotentService idempotentService(com.alicp.jetcache.CacheManager cacheManager, AppProperties appProperties) {
        return new IdempotentService(cacheManager, appProperties);
    }

    @Bean
    @ConditionalOnMissingBean(PermissionService.class)
    public PermissionService permissionService() {
        return new DefaultPermissionService();
    }

    @Bean
    @ConditionalOnMissingBean(JournalService.class)
    public JournalService journalService() {
        return new JournalService();
    }
}