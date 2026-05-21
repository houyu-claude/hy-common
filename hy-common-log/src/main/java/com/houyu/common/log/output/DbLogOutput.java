package com.houyu.common.log.output;

import com.houyu.common.log.model.HyLogEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "hy.log.db", name = "enabled", havingValue = "true")
public class DbLogOutput implements LogOutput {

    private final AsyncDbLogWriter asyncDbLogWriter;

    public DbLogOutput(AsyncDbLogWriter asyncDbLogWriter) {
        this.asyncDbLogWriter = asyncDbLogWriter;
    }

    @Override
    public void output(HyLogEvent event) {
        asyncDbLogWriter.write(event);
    }
}