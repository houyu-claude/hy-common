package com.houyu.common.log.formatter;

import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class TextLogFormatter implements LogFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(HyLogEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
          .append(event.getTimestamp() != null ? event.getTimestamp().format(FORMATTER) : "")
          .append("] [")
          .append(event.getLevel() != null ? event.getLevel().name() : "")
          .append("] [traceId=")
          .append(event.getTraceId() != null ? event.getTraceId() : "")
          .append("] [")
          .append(event.getThreadName() != null ? event.getThreadName() : "")
          .append("] ")
          .append(event.getLoggerName() != null ? event.getLoggerName() : "")
          .append(" - ")
          .append(event.getMessage() != null ? event.getMessage() : "");
        return sb.toString();
    }
}