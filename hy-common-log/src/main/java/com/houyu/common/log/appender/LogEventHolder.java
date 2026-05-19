package com.houyu.common.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;

public class LogEventHolder {
    private ILoggingEvent loggingEvent;

    public ILoggingEvent getLoggingEvent() { return loggingEvent; }
    public void setLoggingEvent(ILoggingEvent e) { this.loggingEvent = e; }
}