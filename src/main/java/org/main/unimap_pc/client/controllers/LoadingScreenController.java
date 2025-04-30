package org.main.unimap_pc.client.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.CheckClientConnection;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadingScreenController implements LanguageSupport {
    @FXML private AnchorPane dragArea;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label loadingText;

    private ScheduledExecutorService connectionCheckService;
    private final WindowDragHandler windowDragHandler = new WindowDragHandler();


    @FXML
    private void initialize() {
        try {
            startLoadingAnimation();
            configureLayout();
            initializeLanguage();
            windowDragHandler.setupWindowDragging(dragArea);
            Platform.runLater(this::startConnectionCheck);
        } catch (Exception e) {
            Logger.error("Error during loading page initializing: " + e.getMessage());
        }
    }

    private void startLoadingAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> loadingText.setText("Loading, please wait")),
                new KeyFrame(Duration.seconds(0.5), e -> loadingText.setText("Loading, please wait.")),
                new KeyFrame(Duration.seconds(1), e -> loadingText.setText("Loading, please wait..")),
                new KeyFrame(Duration.seconds(1.5), e -> loadingText.setText("Loading, please wait..."))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void configureLayout() {
        StackPane.setMargin(loadingIndicator, new Insets(-50, 0, 0, 0));
        StackPane.setMargin(loadingText, new Insets(100, 0, 0, 0));
    }

    private void initializeLanguage() {
        try {
            String defLang = Optional.ofNullable((String) PreferenceServise.get("LANGUAGE")).orElse("en");
            LanguageManager.changeLanguage(defLang);
            LanguageManager.getInstance().registerController(this);
            updateUILanguage(LanguageManager.getCurrentBundle());
        } catch (Exception e) {
            Logger.error("Error during loading page initializing: " + e.getMessage());
        }
    }

    private void startConnectionCheck() {
        connectionCheckService = Executors.newSingleThreadScheduledExecutor();
        Stage stage = (Stage) dragArea.getScene().getWindow();

        connectionCheckService.scheduleAtFixedRate(() ->
                CheckClientConnection.checkConnectionAsync(AppConfig.getCHECK_CONNECTION_URL())
                        .thenAccept(isConnected -> {
                            if (isConnected) {
                                Platform.runLater(() -> {
                                    try {
                                        connectionCheckService.shutdown();
                                        FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConfig.getLOGIN_PAGE_PATH()));
                                        Scene newScene = new Scene(loader.load());
                                        stage.setScene(newScene);
                                    } catch (IOException e) {
                                        Logger.error("Error switching scene: " + e.getMessage());
                                    }
                                });
                            }
                        }), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        loadingText.setText(languageBundle.getString("loading"));
    }

    public static void showLoadScreen(Stage stage) throws IOException {
        showLoadScreen(stage, null);
    }

    public static void showLoadScreen(Stage stage, String message) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(AppConfig.getLOADING_PAGE_PATH()));
        AnchorPane root = loader.load();
        Scene loadingScene = new Scene(root);

        if (message != null) {
            LoadingScreenController controller = loader.getController();
            controller.loadingText.setText(message);
        }

        stage.setScene(loadingScene);
        stage.show();
    }

    public static Parent loadLoginPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource(AppConfig.getLOGIN_PAGE_PATH()));
        return loader.load();
    }
}
