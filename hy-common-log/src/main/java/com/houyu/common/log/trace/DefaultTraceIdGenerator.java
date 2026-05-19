package com.houyu.common.log.trace;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@ConditionalOnBean(MachineIdManager.class)
public class DefaultTraceIdGenerator implements TraceIdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmm");

    private final MachineIdManager machineIdManager;
    private final SequenceGenerator sequenceGenerator = new SequenceGenerator();

    public DefaultTraceIdGenerator(MachineIdManager machineIdManager) {
        this.machineIdManager = machineIdManager;
    }

    @Override
    public String generateTraceId() {
        return generate(null);
    }

    @Override
    public String generateTraceId(String flag) {
        return generate(flag);
    }

    @Override
    public String generateSpanId() {
        return generate(null);
    }

    @Override
    public String generateChildSpanId(String parentSpanId) {
        return generate(null);
    }

    private String generate(String flag) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String machineId = machineIdManager.getMachineId();
        String seq = String.format("%04d", sequenceGenerator.nextSequence(timestamp));
        String flagStr = FlagValidator.normalizeFlag(flag);
        return timestamp + machineId + seq + flagStr;
    }
}