package com.houyu.common.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.houyu.common.log.converter.LogEventConverter;
import com.houyu.common.log.output.LogOutputManager;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.micrometer.core.instrument.Metrics;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class HyCommonLogAppender extends AppenderBase<ILoggingEvent> {

    private Disruptor<LogEventHolder> disruptor;
    private RingBuffer<LogEventHolder> ringBuffer;
    private final int bufferSize;
    private final AtomicLong droppedCount = new AtomicLong(0);
    private final LogEventConverter converter;
    private final LogOutputManager outputManager;

    public HyCommonLogAppender(int bufferSize, LogEventConverter converter, LogOutputManager outputManager) {
        this.bufferSize = bufferSize;
        this.converter = converter;
        this.outputManager = outputManager;
    }

    @Override
    public void start() {
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r, "log-disruptor");
            t.setDaemon(true);
            return t;
        };
        disruptor = new Disruptor<>(
                LogEventHolder::new,
                bufferSize,
                threadFactory,
                ProducerType.MULTI,
                new BlockingWaitStrategy()
        );
        disruptor.handleEventsWith(new LogEventHandler(converter, outputManager));
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }
        eventObject.prepareForDeferredProcessing();

        try {
            long sequence = ringBuffer.tryNext();
            try {
                LogEventHolder holder = ringBuffer.get(sequence);
                holder.setLoggingEvent(eventObject);
            } finally {
                ringBuffer.publish(sequence);
            }
        } catch (InsufficientCapacityException e) {
            long dropped = droppedCount.incrementAndGet();
            Metrics.counter("hy.log.dropped").increment();
            if (dropped % 1000 == 0) {
                addWarn("Log queue full, total dropped: " + dropped);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }
}