package com.houyu.common.log.output;

import com.houyu.common.log.formatter.LogFormatter;
import com.houyu.common.log.model.HyLogEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hy.log.console", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConsoleLogOutput implements LogOutput {

    private final LogFormatter formatter;

    public ConsoleLogOutput(LogFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void output(HyLogEvent event) {
        System.out.println(formatter.format(event));
    }
}