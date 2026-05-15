package com.houyu.common.log.formatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

@Component
public class JsonLogFormatter implements LogFormatter {

    private final ObjectMapper objectMapper;

    public JsonLogFormatter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String format(HyLogEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            return event.getMessage();
        }
    }
}