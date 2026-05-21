package com.houyu.common.log.sampler;

import com.houyu.common.log.config.LogProperties;
import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PercentageLogSampler implements LogSampler {

    private final Random random = new Random();
    private final LogProperties.SamplerConfig samplerConfig;

    public PercentageLogSampler(LogProperties logProperties) {
        this.samplerConfig = logProperties.getSampler();
    }

    @Override
    public boolean sample(HyLogEvent event) {
        if (!samplerConfig.isEnabled()) {
            return true;
        }
        double samplingRate = samplerConfig.getRate();
        if (samplingRate >= 1.0) {
            return true;
        }
        if (samplingRate <= 0) {
            return false;
        }
        return random.nextDouble() < samplingRate;
    }
}