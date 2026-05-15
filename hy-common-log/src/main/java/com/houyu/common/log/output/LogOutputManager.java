package com.houyu.common.log.output;

import com.houyu.common.log.desensitizer.DesensitizerManager;
import com.houyu.common.log.filter.LogFilter;
import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.sampler.LogSampler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogOutputManager {

    private final List<LogFilter> filters;
    private final List<LogSampler> samplers;
    private final List<LogOutput> outputs;
    private final DesensitizerManager desensitizerManager;

    public LogOutputManager(List<LogFilter> filters, List<LogSampler> samplers,
                           List<LogOutput> outputs, DesensitizerManager desensitizerManager) {
        this.filters = filters;
        this.samplers = samplers;
        this.outputs = outputs;
        this.desensitizerManager = desensitizerManager;
    }

    public void output(HyLogEvent event) {
        if (!filter(event)) {
            return;
        }
        if (!sample(event)) {
            return;
        }
        desensitize(event);
        for (LogOutput output : outputs) {
            output.output(event);
        }
    }

    private boolean filter(HyLogEvent event) {
        for (LogFilter filter : filters) {
            if (!filter.filter(event)) {
                return false;
            }
        }
        return true;
    }

    private boolean sample(HyLogEvent event) {
        for (LogSampler sampler : samplers) {
            if (!sampler.sample(event)) {
                return false;
            }
        }
        return true;
    }

    private void desensitize(HyLogEvent event) {
        if (event.getMessage() != null) {
            event.setMessage(desensitizerManager.desensitize(event.getMessage()));
        }
        if (event.getFormattedMessage() != null) {
            event.setFormattedMessage(desensitizerManager.desensitize(event.getFormattedMessage()));
        }
    }
}