package org.main.unimap_pc.client.utils;

import org.main.unimap_pc.client.configs.AppConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class Logger {
    public enum LevelofLogs {
        INFO,
        WARNING,
        ERROR
    }

    private static final LevelofLogs CONFIGURED_LEVEL = LevelofLogs.valueOf(AppConfig.getLogLevel().toUpperCase());
    private static int userId = -1;
    private static final String serverUrl = AppConfig.getLogPagePath();
    private static boolean logToConsole = false;
    private static final int MAX_THREADS = 2;
    private static final int MAX_QUEUE_SIZE = 50;

    public static void init(int userIdFromLogin) {
        userId = userIdFromLogin;
    }

    private static void log(LevelofLogs level, String message) {
        if (level.ordinal() < CONFIGURED_LEVEL.ordinal()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMessage = String.format("[%s] [%s] %s%n", timestamp, level, message);

        if (logToConsole) {
            System.out.print(logMessage);
        }

        try {
            executor.submit(() -> sendToServer(timestamp, level.toString(), message));
        } catch (RejectedExecutionException e) {
            if (logToConsole) {
                System.err.println("LOGGING DROPPED: Logging system is overloaded. Dropping log: " + message);
            }
        }
    }

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            MAX_THREADS,
            MAX_THREADS,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_QUEUE_SIZE),
            new ThreadPoolExecutor.AbortPolicy() // відкидає задачі, якщо черга заповнена
    );

    private static void sendToServer(String timestamp, String level, String message) {
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"userId\": %d, \"timestamp\": \"%s\", \"level\": \"%s\", \"message\": \"%s\"}",
                    userId, timestamp, level, message
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            // Зчитування відповіді сервера
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 200 && responseCode < 400
                                    ? conn.getInputStream()
                                    : conn.getErrorStream()
                    )
            )) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                if (responseCode != 200) {
                    if (logToConsole) {
                        System.err.println("LOGGING ERROR: Failed to send log to server. Response code: " + responseCode);
                        System.err.println("Server response: " + response);
                    }
                } else {
                    if (logToConsole) {
                        System.out.println("Log successfully sent. Server response: " + response);
                    }
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            if (logToConsole) {
                System.err.println("LOGGING ERROR: " + e.getMessage());
            }
        }
    }

    public static void shutdown() {
        executor.shutdown();
    }

    public static void info(String message) {
        log(LevelofLogs.INFO, message);
    }
    public static void warning(String message) {
        log(LevelofLogs.WARNING, message);
    }
    public static void error(String message) {
        log(LevelofLogs.ERROR, message);
    }
    public static void main(String[] args) {

    }
}