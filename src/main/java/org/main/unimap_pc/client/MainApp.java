package org.main.unimap_pc.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.AuthService;
import org.main.unimap_pc.client.services.CheckClientConnection;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.controllers.LoadingScreenController;
import org.main.unimap_pc.client.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.main.unimap_pc.client.utils.ErrorScreens.showErrorScreen;

public class MainApp extends Application {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final UserService userService = UserService.getInstance();

    // Check if cache files exist
    private boolean areCacheFilesPresent() {
        String[] cacheFilesPath = {AppConfig.getPREFS_FILE(), AppConfig.getCACHE_FILE()};
        return Stream.of(cacheFilesPath)
                .map(File::new)
                .anyMatch(file -> file.exists() && file.isFile() && file.length() > 0);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle(AppConfig.getAPP_TITLE());
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        loadFonts();

        LoadingScreenController.showLoadScreen(stage);

        String refreshToken = (String) PreferenceServise.get("REFRESH_TOKEN");

        checkConnectionAndProceed(stage, refreshToken);
    }

    private void checkConnectionAndProceed(Stage stage, String refreshToken) {
        CheckClientConnection.checkConnectionAsync(AppConfig.getCHECK_CONNECTION_URL())
                .thenAccept(isServerConnected -> {
                    boolean hasCacheFiles = areCacheFilesPresent();

                    Platform.runLater(() -> handleConnection(stage, refreshToken, isServerConnected, hasCacheFiles));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> handleConnectionOnFailure(stage, refreshToken));
                    return null;
                });
    }

    private void handleConnection(Stage stage, String refreshToken, boolean isServerConnected, boolean hasCacheFiles) {
        try {
            if (hasCacheFiles && !isServerConnected) {
                // Кеш есть, интернета нет: автологин без проверки
                if (refreshToken != null) userService.autoLogin(stage);
                else showLoadingScreen(stage, "Нет подключения к интернету");
            } else if (hasCacheFiles && isServerConnected) {
                // Кеш есть, интернет есть: полный автологин
                if (refreshToken != null) {
                    AuthService.refreshAccessToken().thenAccept(isTokenRefreshed -> {
                        if (isTokenRefreshed) {
                            try {
                                userService.autoLogin(stage);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else handleLoginPageFallback(stage);
                    });
                } else handleLoginPageFallback(stage);
            } else if (!hasCacheFiles && isServerConnected) {
                // Кеша нет, интернет есть: открываем страницу логина
                showLoginPage(stage);
            } else {
                showLoadingScreen(stage, "Нет подключения к серверу");
            }
        } catch (IOException e) {
            Logger.error("Error during loading the application: "+ e);
            showErrorScreen("Error during loading the application");
        }
    }

    private void handleConnectionOnFailure(Stage stage, String refreshToken) {
        try {
            boolean hasCacheFiles = areCacheFilesPresent();
            if (hasCacheFiles && refreshToken != null) {
                userService.autoLogin(stage);
            } else {
                showLoadingScreen(stage, "Нет подключения к серверу");
            }
        } catch (IOException e) {
            Logger.error("Error during loading the application: "+ e);
            showErrorScreen("Ошибка при загрузке приложения");
        }
    }

    private void handleLoginPageFallback(Stage stage) {
        Platform.runLater(() -> showLoginPage(stage));
    }

    private void showLoginPage(Stage stage) {
        try {
            Scene loginScene = new Scene(LoadingScreenController.loadLoginPage());
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            Logger.error("Failed to load login page: "+ e);
            showErrorScreen("Failed to load login page");
        }
    }

    private void showLoadingScreen(Stage stage, String message) {
        Platform.runLater(() -> {
            try {
                LoadingScreenController.showLoadScreen(stage, message);
            } catch (IOException e) {
                Logger.error("Failed to load the loading screen: "+ e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void stop() {
        executorService.shutdown();
        scheduler.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.info("ExecutorService did not terminate in time, forcing shutdown.");
                executorService.shutdownNow();
            }
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.info("Scheduler did not terminate in time, forcing shutdown.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Logger.error("Thread interrupted while shutting down executors: "+ e);
            executorService.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void loadFonts() {
        String fontsDir = "/org/main/unimap_pc/views/style/fonts";
        try {
            // Get all font files in the directory
            var fontResources = getClass().getResource(fontsDir);
            if (fontResources == null) {
                Logger.error("Font directory not found: " + fontsDir);
                return;
            }

            // Load each font file
            try (Stream<Path> paths = Files.walk(Paths.get(fontResources.toURI()))) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    try (var fontStream = getClass().getResourceAsStream(fontsDir + "/" + path.getFileName())) {
                        if (fontStream != null) {
                            Font.loadFont(fontStream, 10);
                            Logger.info("Loaded font: " + path.getFileName());
                        } else {
                            Logger.error("Font file not found: " + path.getFileName());
                        }
                    } catch (IOException e) {
                        Logger.error("Failed to load font: " + path.getFileName() + " - " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            Logger.error("Failed to load fonts from directory: " + fontsDir + " - " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
