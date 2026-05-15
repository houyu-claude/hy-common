package com.houyu.common.log.trace;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SequenceGenerator {

    private static final int MAX_SEQUENCE = 9999;
    private final AtomicInteger sequence = new AtomicInteger(0);
    private volatile String lastResetTimestamp = "";
    private final ReentrantLock resetLock = new ReentrantLock();

    public int nextSequence(String currentTimestamp) {
        if (!currentTimestamp.equals(lastResetTimestamp)) {
            resetIfNeeded(currentTimestamp);
        }
        return Math.abs(sequence.getAndIncrement()) % (MAX_SEQUENCE + 1);
    }

    private void resetIfNeeded(String currentTimestamp) {
        if (resetLock.tryLock()) {
            try {
                if (!currentTimestamp.equals(lastResetTimestamp)) {
                    sequence.set(0);
                    lastResetTimestamp = currentTimestamp;
                }
            } finally {
                resetLock.unlock();
            }
        }
    }
}