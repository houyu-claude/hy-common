package com.houyu.common.log.filter;

import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class KeywordLogFilter implements LogFilter {

    private static final List<String> FILTER_KEYWORDS = Arrays.asList("password", "secret", "token", "credential");

    @Override
    public boolean filter(HyLogEvent event) {
        if (event.getMessage() == null) {
            return true;
        }
        String message = event.getMessage().toLowerCase();
        for (String keyword : FILTER_KEYWORDS) {
            if (message.contains(keyword.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}