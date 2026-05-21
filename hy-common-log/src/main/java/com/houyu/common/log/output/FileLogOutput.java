package com.houyu.common.log.output;

import com.houyu.common.log.config.LogProperties;
import com.houyu.common.log.formatter.LogFormatter;
import com.houyu.common.log.model.HyLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
@ConditionalOnProperty(prefix = "hy.log.file", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FileLogOutput implements LogOutput {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger logger = LoggerFactory.getLogger(FileLogOutput.class);

    private final LogFormatter formatter;
    private final String logPath;
    private final String fileName;
    private final BlockingQueue<String> queue;
    private final ScheduledExecutorService scheduler;

    public FileLogOutput(LogFormatter formatter, LogProperties logProperties) {
        this.formatter = formatter;
        this.logPath = logProperties.getFile().getPath();
        this.fileName = logProperties.getFile().getName();
        this.queue = new LinkedBlockingQueue<>(65536);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "log-file-writer");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this::flush, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void output(HyLogEvent event) {
        String logLine = formatter.format(event) + "\n";
        if (!queue.offer(logLine)) {
            logger.warn("File log queue full, dropping log");
        }
    }

    private void flush() {
        List<String> lines = new ArrayList<>();
        queue.drainTo(lines);
        if (lines.isEmpty()) return;
        
        try {
            Path logDir = Paths.get(logPath);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            String fullFileName = fileName.replace(".log", "") + "-" + LocalDate.now().format(DATE_FMT) + ".log";
            Path logFile = logDir.resolve(fullFileName);
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
            }
            Files.writeString(logFile, sb.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            logger.error("Failed to write log to file", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        flush();
    }
}