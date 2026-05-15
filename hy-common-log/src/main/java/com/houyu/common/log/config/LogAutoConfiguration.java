package com.houyu.common.log.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.houyu.common.log.appender.HyCommonLogAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(LogProperties.class)
@ConditionalOnProperty(prefix = "hy.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogAutoConfiguration {

    private final LogProperties logProperties;

    public LogAutoConfiguration(LogProperties logProperties) {
        this.logProperties = logProperties;
    }

    @PostConstruct
    public void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        HyCommonLogAppender appender = new HyCommonLogAppender(
                logProperties.getAppender().getBufferSize()
        );
        appender.setContext(loggerContext);
        appender.setName(logProperties.getAppender().getName());
        appender.start();

        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        if (rootLogger.getAppender(logProperties.getAppender().getName()) == null) {
            rootLogger.addAppender(appender);
        }
    }
}