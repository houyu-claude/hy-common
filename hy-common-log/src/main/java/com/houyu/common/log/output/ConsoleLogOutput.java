package com.houyu.common.log.output;

import com.houyu.common.log.formatter.LogFormatter;
import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

@Component
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