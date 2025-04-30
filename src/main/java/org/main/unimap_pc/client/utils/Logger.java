package org.main.unimap_pc.client.utils;

import org.main.unimap_pc.client.configs.AppConfig;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Logger {
    public enum LevelofLogs {
        INFO,
        WARNING,
        ERROR
    }

    private static final LevelofLogs CONFIGURED_LEVEL = LevelofLogs.valueOf(AppConfig.getLogLevel().toUpperCase());
    private static int userId = 120;
    private static final String serverUrl = AppConfig.getLOG_URL();
    private static boolean logToConsole = false;
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public Logger(int userId) {
        this.userId = userId;
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

        executor.submit(() -> sendToServer(timestamp, level.toString(), message));
    }

    private static void sendToServer(String timestamp, String level, String message) {
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            if (responseCode != 200) {
                System.err.println("LOGGING ERROR: Failed to send log to server. Response code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            System.err.println("LOGGING ERROR: " + e.getMessage());
        }
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