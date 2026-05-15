package com.houyu.common.log.filter;

import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.model.LogLevel;
import org.springframework.stereotype.Component;

@Component
public class LevelLogFilter implements LogFilter {

    @Override
    public boolean filter(HyLogEvent event) {
        if (event.getLevel() == null) {
            return true;
        }
        return switch (event.getLevel()) {
            case TRACE -> true;
            case DEBUG -> true;
            case INFO -> true;
            case WARN -> true;
            case ERROR -> true;
        };
    }
}