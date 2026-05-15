package com.houyu.common.log.appender;

import com.houyu.common.log.converter.LogEventConverter;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.output.LogOutputManager;
import com.lmax.disruptor.EventHandler;

public class LogEventHandler implements EventHandler<LogEventHolder> {

    private final LogEventConverter converter;
    private final LogOutputManager outputManager;

    public LogEventHandler(LogEventConverter converter, LogOutputManager outputManager) {
        this.converter = converter;
        this.outputManager = outputManager;
    }

    @Override
    public void onEvent(LogEventHolder holder, long sequence, boolean endOfBatch) {
        HyLogEvent hyLogEvent = converter.convert(holder.getLoggingEvent());
        outputManager.output(hyLogEvent);
    }
}