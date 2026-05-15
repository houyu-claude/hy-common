package com.houyu.common.log.formatter;

import com.houyu.common.log.model.HyLogEvent;

public interface LogFormatter {
    String format(HyLogEvent event);
}