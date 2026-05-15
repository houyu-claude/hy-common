package com.houyu.common.log.output;

import com.houyu.common.log.formatter.LogFormatter;
import com.houyu.common.log.model.HyLogEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class FileLogOutput implements LogOutput {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String LOG_DIR = "./logs";

    private final LogFormatter formatter;

    public FileLogOutput(LogFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void output(HyLogEvent event) {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            String fileName = "hy-common-" + LocalDate.now().format(DATE_FMT) + ".log";
            Path logFile = logDir.resolve(fileName);
            String logLine = formatter.format(event) + "\n";
            Files.writeString(logFile, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}