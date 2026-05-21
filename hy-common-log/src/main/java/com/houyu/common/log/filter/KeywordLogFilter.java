package com.houyu.common.log.filter;

import com.houyu.common.log.config.LogProperties;
import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class KeywordLogFilter implements LogFilter {

    private static final List<String> DEFAULT_KEYWORDS = Arrays.asList("password", "secret", "token", "credential");
    
    private final LogProperties.FilterConfig filterConfig;

    public KeywordLogFilter(LogProperties logProperties) {
        this.filterConfig = logProperties.getFilter();
    }

    @Override
    public boolean filter(HyLogEvent event) {
        if (!filterConfig.isEnabled()) {
            return true;
        }
        if (event.getMessage() == null) {
            return true;
        }
        List<String> keywords = filterConfig.getKeywords();
        if (keywords == null || keywords.isEmpty()) {
            keywords = DEFAULT_KEYWORDS;
        }
        String message = event.getMessage().toLowerCase();
        for (String keyword : keywords) {
            if (message.contains(keyword.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}