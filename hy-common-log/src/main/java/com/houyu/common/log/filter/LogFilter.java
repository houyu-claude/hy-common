package com.houyu.common.log.filter;

import com.houyu.common.log.model.HyLogEvent;

public interface LogFilter {
    boolean filter(HyLogEvent event);
}