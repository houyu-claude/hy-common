package com.houyu.common.log.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.houyu.common.log.appender.HyCommonLogAppender;
import com.houyu.common.log.converter.LogEventConverter;
import com.houyu.common.log.output.LogOutputManager;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(LogProperties.class)
@ConditionalOnProperty(prefix = "hy.log", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class LogAutoConfiguration {

    private final LogProperties logProperties;
    private final LogEventConverter converter;
    private final LogOutputManager outputManager;

    public LogAutoConfiguration(LogProperties logProperties, 
                               LogEventConverter converter, 
                               LogOutputManager outputManager) {
        this.logProperties = logProperties;
        this.converter = converter;
        this.outputManager = outputManager;
    }

    @PostConstruct
    public void init() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        HyCommonLogAppender appender = new HyCommonLogAppender(
                logProperties.getAppender().getBufferSize(),
                converter,
                outputManager
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