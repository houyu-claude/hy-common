package com.houyu.common.log.sampler;

import com.houyu.common.log.model.HyLogEvent;

public interface LogSampler {
    boolean sample(HyLogEvent event);
}