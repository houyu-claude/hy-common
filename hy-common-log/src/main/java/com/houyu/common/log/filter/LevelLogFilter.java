package com.houyu.common.log.filter;

import com.houyu.common.log.config.LogProperties;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.model.LogLevel;
import org.springframework.stereotype.Component;

@Component
public class LevelLogFilter implements LogFilter {

    private final LogProperties.FilterConfig filterConfig;

    public LevelLogFilter(LogProperties logProperties) {
        this.filterConfig = logProperties.getFilter();
    }

    @Override
    public boolean filter(HyLogEvent event) {
        if (!filterConfig.isEnabled()) {
            return true;
        }
        if (event.getLevel() == null) {
            return true;
        }
        LogLevel minLevel = parseLevel(filterConfig.getMinLevel());
        return event.getLevel().ordinal() >= minLevel.ordinal();
    }

    private LogLevel parseLevel(String levelStr) {
        try {
            return LogLevel.valueOf(levelStr.toUpperCase());
        } catch (Exception e) {
            return LogLevel.TRACE;
        }
    }
}