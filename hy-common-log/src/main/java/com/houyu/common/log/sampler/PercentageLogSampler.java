package com.houyu.common.log.sampler;

import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PercentageLogSampler implements LogSampler {

    private static final double DEFAULT_SAMPLING_RATE = 1.0;
    private final Random random = new Random();

    @Override
    public boolean sample(HyLogEvent event) {
        double samplingRate = getSamplingRate(event);
        return random.nextDouble() < samplingRate;
    }

    private double getSamplingRate(HyLogEvent event) {
        return DEFAULT_SAMPLING_RATE;
    }
}