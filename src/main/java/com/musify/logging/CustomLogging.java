package com.musify.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class CustomLogging {
    private static final String LOG_DIRECTORY = "logs/";
    private static final String BASE_NAME = "musify_log";
    private static final String FILE_EXTENSION = ".txt";

    private static enum LogType {
        INFO,
        WARN,
        ERROR
    }

    public void info(String className, String methodName, String message) {
        addLog(LogType.INFO, className, methodName, message);
    }

    public void warn(String className, String methodName, String message) {
        addLog(LogType.WARN, className, methodName, message);
    }

    public void error(String className, String methodName, String message) {
        addLog(LogType.ERROR, className, methodName, message);
    }

    private void addLog(LogType logType, String className, String methodName, String message) {
        if (className.isBlank() || methodName.isBlank() || message.isBlank())
            return;

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTextFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = dateTime.format(dateTextFormat);

        String logMessage = String.format("[%s] - %s - %s - %s - %s",
                formattedDateTime, logType.name(), className, methodName, message);

        try {
            Files.createDirectories(Paths.get(LOG_DIRECTORY));
        } catch (IOException e) {
        }

        DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String fileTimestamp = dateTime.format(fileFormatter);

        String logFileName = String.format("%s%s%s",
                fileTimestamp, BASE_NAME, FILE_EXTENSION);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(LOG_DIRECTORY + logFileName, true))) {

            writer.write(logMessage);
            writer.newLine();
        } catch (IOException e) {
        }
    }
}
