package org.main.unimap_pc.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.Getter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.controllers.SceneController;
import org.main.unimap_pc.client.services.AuthService;
import org.main.unimap_pc.client.services.CheckClientConnection;

import org.main.unimap_pc.client.utils.LoadingScreens;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.controllers.LoadingScreenController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.main.unimap_pc.client.utils.ErrorScreens.showErrorScreen;

public class MainApp extends Application {
    @Getter
    private static SceneController sceneController;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final UserService userService = UserService.getInstance();

    // Метод для проверки наличия кеш-файлов
    private boolean areCacheFilesPresent() {
        String[] cacheFilesPath = {AppConfig.getPREFS_FILE(), AppConfig.getCACHE_FILE()};

        for (String cacheFilePath : cacheFilesPath) {
            File cacheFile = new File(cacheFilePath);
            if (cacheFile.exists() && cacheFile.isFile() && cacheFile.length() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle(AppConfig.getAppTitle());
        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);

        loadFonts();

        sceneController = new SceneController(stage);
        LoadingScreenController.showLoadScreen(stage);

        UserService userService = UserService.getInstance();
        String refreshToken = (String) PreferenceServise.get("REFRESH_TOKEN");

        // Проверяем наличие кеша и соединения с сервером
        checkConnectionAndProceed(stage, refreshToken);
    }

    private void checkConnectionAndProceed(Stage stage, String refreshToken) {
        CheckClientConnection.checkConnectionAsync(AppConfig.getCheckConnectionUrl())
                .thenAccept(isServerConnected -> {
                    boolean hasCacheFiles = areCacheFilesPresent();

                    Platform.runLater(() -> {
                        try {
                            if (hasCacheFiles && !isServerConnected) {
                                // Есть кеш, но нет интернета - автологин без проверки
                                if (refreshToken != null) {
                                    userService.autoLogin(stage);
                                } else {
                                    showLoadingScreen(stage, "Нет подключения к интернету");
                                }
                            } else if (hasCacheFiles && isServerConnected) {
                                // Есть кеш и интернет - полный автологин
                                if (refreshToken != null) {
                                    AuthService.refreshAccessToken().thenAccept(isTokenRefreshed -> {
                                        if (isTokenRefreshed) {
                                            try {
                                                userService.autoLogin(stage);
                                            } catch (IOException e) {
                                                handleLoginPageFallback(stage);
                                            }
                                        } else {
                                            handleLoginPageFallback(stage);
                                        }
                                    });
                                } else {
                                    handleLoginPageFallback(stage);
                                }
                            } else if (!hasCacheFiles && isServerConnected) {
                                // Нет кеша, есть интернет - открываем страницу логина
                                sceneController.changeScene(AppConfig.getLoginPagePath());
                            } else {
                                // Нет кеша, нет интернета - показываем экран загрузки
                                showLoadingScreen(stage, "Нет подключения к серверу");
                            }
                        } catch (IOException e) {
                            Logger.error("Error during loading the application" + e.getMessage());
                            showErrorScreen("Error during loading the application");
                        }
                    });
                })
                .exceptionally(ex -> {
                    // В случае ошибки подключения
                    Platform.runLater(() -> {
                        try {
                            boolean hasCacheFiles = areCacheFilesPresent();

                            if (hasCacheFiles && refreshToken != null) {
                                // Есть кеш - автологин без проверки
                                userService.autoLogin(stage);
                            } else {
                                // Нет кеша - показываем экран загрузки
                                showLoadingScreen(stage, "Нет подключения к серверу");
                            }
                        } catch (IOException e) {
                            Logger.error("Error during loading the application" + e.getMessage());
                            showErrorScreen("Ошибка при загрузке приложения");
                        }
                    });
                    return null;
                });
    }

    private void handleLoginPageFallback(Stage stage) {
        Platform.runLater(() -> {
            try {
                sceneController.changeScene(AppConfig.getLoginPagePath());
            } catch (IOException e) {
                Logger.error("Failed to load login page: " + e.getMessage());
                showErrorScreen("Failed to load login page");
            }
        });
    }

    private void showLoadingScreen(Stage stage, String message) {
        Platform.runLater(() -> {
            try {
                LoadingScreenController.showLoadScreen(stage, message);
            } catch (IOException e) {
                Logger.error("Failed to load the loading screen: " + e.getMessage());
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
                Logger.warning("ExecutorService did not terminate in time, forcing shutdown.");
                executorService.shutdownNow();
            }
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.warning("Scheduler did not terminate in time, forcing shutdown.");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Logger.error("Thread interrupted while shutting down executors: " + e.getMessage());
            executorService.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void loadFonts() {
        String fontsDir = "org/main/unimap_pc/views/style/fonts";
        try (Stream<Path> paths = Files.walk(Paths.get(getClass().getClassLoader().getResource(fontsDir).toURI()))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    Font.loadFont(Files.newInputStream(path), 10);
                    System.out.println("Loaded font: " + path.getFileName());
                } catch (IOException e) {
                    Logger.error("Failed to load font: " + path.getFileName() + " - " + e.getMessage());
                    System.err.println("Failed to load font: " + path.getFileName() + " - " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Logger.error("Failed to load fonts from directory: " + fontsDir + " - " + e.getMessage());
            System.err.println("Failed to load fonts from directory: " + fontsDir + " - " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}