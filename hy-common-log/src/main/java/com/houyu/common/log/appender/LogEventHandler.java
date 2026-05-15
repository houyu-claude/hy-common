package com.houyu.common.log.appender;

import com.houyu.common.log.converter.LogEventConverter;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogEventHandler implements EventHandler<LogEventHolder> {

    private static final Logger logger = LoggerFactory.getLogger(LogEventHandler.class);

    private volatile LogEventConverter converter;
    private volatile LogOutputManager outputManager;

    public LogEventHandler() {
    }

    public LogEventHandler(LogEventConverter converter, LogOutputManager outputManager) {
        this.converter = converter;
        this.outputManager = outputManager;
    }

    public void setConverter(LogEventConverter converter) {
        this.converter = converter;
    }

    public void setOutputManager(LogOutputManager outputManager) {
        this.outputManager = outputManager;
    }

    @Override
    public void onEvent(LogEventHolder holder, long sequence, boolean endOfBatch) {
        if (converter == null || outputManager == null) {
            logger.warn("LogEventHandler not fully initialized, skipping log event");
            return;
        }
        HyLogEvent hyLogEvent = converter.convert(holder.getLoggingEvent());
        outputManager.output(hyLogEvent);
    }
}