package com.houyu.common.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "hy.app.enabled", havingValue = "true", matchIfMissing = true)
public class AppAutoConfiguration {
}