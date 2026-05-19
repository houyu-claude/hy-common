package com.houyu.common.log.output;

import com.houyu.common.log.model.HyLogEvent;

public interface LogOutput {
    void output(HyLogEvent event);
}