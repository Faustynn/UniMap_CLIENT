package org.main.unimap_pc.client.utils;

import lombok.experimental.UtilityClass;
import org.main.unimap_pc.client.configs.AppConfig;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;

@UtilityClass
public class ParserUtil {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // for debug start after 5 seconds once
    public void startParsingScheduleDebug() {
        scheduler.scheduleAtFixedRate(
                () -> runPythonParserTask(),
                5,
                1,
                TimeUnit.HOURS
        );
        Logger.info("Scheduled parsing started. Next run: " + LocalDateTime.now().plusSeconds(5));
    }


    public void startParsingSchedule() {

        // run at 4:00 every day
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.with(LocalTime.of(4, 0));

        if (now.toLocalTime().isAfter(LocalTime.of(4, 0))) {
            nextRun = nextRun.plusDays(1);
        }

        // Calculate delay until first run
        Duration duration = Duration.between(now, nextRun);
        long initialDelay = duration.getSeconds();

        scheduler.scheduleAtFixedRate(
                () -> runPythonParserTask(),
                initialDelay,
                TimeUnit.DAYS.toSeconds(1),
                TimeUnit.SECONDS
        );

        Logger.info("Scheduled parsing started. Next run: " + nextRun);
    }


    private void runPythonParserTask() {
        try {
            runPythonScript();
            Logger.info("Parsing completed: " + LocalDateTime.now());
        } catch (Exception e) {
            Logger.error("Error during parsing task execution " + e);
        }
    }

    private void runPythonScript() {
        String pythonFilePath = AppConfig.getPARTHER_FILE();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFilePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            System.out.println("Python script finished with code: " + exitCode);
            Logger.info("Python script finished with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            Logger.error("Error when launching Python script " + e);
            Thread.currentThread().interrupt();
        }
    }
}