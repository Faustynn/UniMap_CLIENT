package org.main.unimap_pc.client.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.CheckClientConnection;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadingScreenController implements LanguageSupport {

    @FXML
    private AnchorPane dragArea;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private ProgressIndicator loadingIndicator;
    @FXML
    private Label loadingText;
    private String defLang;

    private ScheduledExecutorService connectionCheckService;
    private SceneController sceneController;

    @FXML
    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) dragArea.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    private void initialize() {
        // Existing loading animation setup
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> loadingText.setText("Loading, please wait")),
                new KeyFrame(Duration.seconds(0.5), event -> loadingText.setText("Loading, please wait.")),
                new KeyFrame(Duration.seconds(1), event -> loadingText.setText("Loading, please wait..")),
                new KeyFrame(Duration.seconds(1.5), event -> loadingText.setText("Loading, please wait..."))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        StackPane.setMargin(loadingIndicator, new Insets(-50, 0, 0, 0));
        StackPane.setMargin(loadingText, new Insets(100, 0, 0, 0));

        try {
            defLang = PreferenceServise.get("LANGUAGE").toString();
            LanguageManager.changeLanguage(defLang);
            LanguageManager.getInstance().registerController(this);
            updateUILanguage(LanguageManager.getCurrentBundle());

            // Start periodic server connection check
            Platform.runLater(this::startConnectionCheck);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startConnectionCheck() {
        connectionCheckService = Executors.newSingleThreadScheduledExecutor();
        Stage currentStage = (Stage) dragArea.getScene().getWindow();

        connectionCheckService.scheduleAtFixedRate(() -> {
            CheckClientConnection.checkConnectionAsync(AppConfig.getCheckConnectionUrl())
                    .thenAccept(isConnected -> {
                        if (isConnected) {
                            Platform.runLater(() -> {
                                try {
                                    // Stop checking connection
                                    connectionCheckService.shutdown();

                                    // Initialize SceneController if not already done
                                    if (sceneController == null) {
                                        sceneController = new SceneController(currentStage);
                                    }

                                    // Change to login scene
                                    sceneController.changeScene(AppConfig.getLoginPagePath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
        }, 0, 5, TimeUnit.SECONDS);  // Check every 5 seconds
    }

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        loadingText.setText(languageBundle.getString("loading"));
    }

    public static void showLoadScreen(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource("/org/main/unimap_pc/views/LoadingScreen.fxml"));
        AnchorPane root = loader.load();
        Scene loadingScene = new Scene(root);
        stage.setScene(loadingScene);
        stage.show();
    }

    public static void showLoadScreen(Stage stage, String message) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoadingScreenController.class.getResource("/org/main/unimap_pc/views/LoadingScreen.fxml"));
        AnchorPane root = loader.load();
        LoadingScreenController controller = loader.getController();
        controller.loadingText.setText(message);
        Scene loadingScene = new Scene(root);
        stage.setScene(loadingScene);
        stage.show();
    }

}