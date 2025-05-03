package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.*;
import org.main.unimap_pc.client.utils.*;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogInController implements LanguageSupport {

    @FXML private AnchorPane dragArea;
    @FXML private FontAwesomeIcon closeApp;
    @FXML private TextField fieldUsername;
    @FXML private PasswordField fieldPassword;
    @FXML private Button btnSignin, btnSignup, btnGoogle, btnFacebook;
    @FXML private Label infoMess, btnForgotPass, madeby, downlApp, dontHaveAcc, or;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private CheckBox remember_checkBox, checkTerms;

    private final SecurityService securityService = new SecurityService();
    private ScheduledExecutorService connectionCheckService;
    private final WindowDragHandler windowDragHandler = new WindowDragHandler();
    private final SseManager sseManager = new SseManager();
    private static final String CURRENT_PAGE = AppConfig.getLOGIN_PAGE_PATH();

    @FXML
    private void initialize() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        PreferenceServise.put(AppConfig.getLANGUAGE_KEY(), "Language");
        initLanguage();
        windowDragHandler.setupWindowDragging(dragArea);
        LanguageManager.getInstance().registerController(this);
        Platform.runLater(this::startConnectionMonitoring);
    }

    private void initLanguage() {
        String selectedLang = String.valueOf(PreferenceServise.get(AppConfig.getLANGUAGE_KEY()));
        languageComboBox.setValue(selectedLang);
        languageComboBox.setOnAction(event -> {
            try {
                String langCode = AppConfig.getLANGUAGE_CODES().get(languageComboBox.getValue());
                LanguageManager.changeLanguage(langCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                Logger.error("Error changing language: "+ e.getMessage());
                showErrorDialog("Error changing language: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleSignIn() {
        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            infoMess.setText("Please enter your username and password!");
            return;
        }

        if (!securityService.checkNames(username) || !securityService.checkPassword(password)) {
            infoMess.setText("Invalid username or password format!");
            return;
        }

        AuthService.login(username, password).thenAccept(success -> Platform.runLater(() -> {
            if (success) {
                stopConnectionCheck();
                sseManager.connectToSSEServer();
                switchToMainPage();
            } else {
                infoMess.setText("Failed to log in. Please check your username and password.");

                CheckClientConnection.checkConnectionAsync(AppConfig.getCHECK_CONNECTION_URL())
                        .thenAccept(connected -> {
                            if (!connected) {
                                Platform.runLater(this::handleLostConnection);
                            }
                        });
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                infoMess.setText("Login request failed.");
                CheckClientConnection.checkConnectionAsync(AppConfig.getCHECK_CONNECTION_URL())
                        .thenAccept(connected -> {
                            if (!connected) handleLostConnection();
                        });
            });
            return null;
        });
    }

    private void switchToMainPage() {
        try {
            Stage stage = (Stage) btnSignin.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getMAIN_PAGE_PATH())));
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            Logger.error("Main page loading failed: "+ e.getMessage());
            showErrorDialog("Main page loading failed.");
        }
    }

    @FXML
    private void handleSignUp() {
        openModal(AppConfig.getSIGNUP_PAGE_PATH(), "Sign Up", "Sign up window failed");
    }

    @FXML
    private void handleForgotPass() {
        openModal(AppConfig.getFORGOT_PASS_PAGE_PATH(), "Forgot Password", "Forgot password window failed");
    }

    private void openModal(String fxml, String title, String errorMessage) {
        try {
            Stage parentStage = (Stage) dragArea.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            loader.setResources(LanguageManager.getCurrentBundle());
            AnchorPane pane = loader.load();

            Stage modal = new Stage(StageStyle.TRANSPARENT);
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(parentStage);
            modal.setTitle(title);
            modal.setScene(new Scene(pane));

            StackPane overlay = createOverlay(parentStage);
            ((AnchorPane) parentStage.getScene().getRoot()).getChildren().add(overlay);
            modal.setOnHidden(e -> ((AnchorPane) parentStage.getScene().getRoot()).getChildren().remove(overlay));
            modal.showAndWait();

        } catch (IOException e) {
            Logger.error(errorMessage +": "+ e.getMessage());
            showErrorDialog(errorMessage);
        }
    }

    private StackPane createOverlay(Stage parentStage) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        overlay.setPrefSize(parentStage.getWidth(), parentStage.getHeight());
        overlay.setOnMouseClicked(e -> Toolkit.getDefaultToolkit().beep());
        return overlay;
    }

    @FXML
    private void handleDownlApp() {
        openUrl(AppConfig.getGIT_HUB(), "GitHub");
    }

    @FXML
    private void handleSignByGoogle() {
        openUrl(AppConfig.getOAUTH2_GOOGLE(), "Google Auth");
    }

    @FXML
    private void handleSignByFacebook() {
        openUrl(AppConfig.getOAUTH2_FACEBOOK(), "Facebook Auth");
    }

    private void openUrl(String url, String name) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            Logger.error("Failed to open"+ name + ": "+ e.getMessage());
            ErrorScreens.showErrorScreen("Failed to open " + name);
        }
    }

    @FXML
    private void handleCheckBox() {
        PreferenceServise.put("REMEMBER", remember_checkBox.isSelected());
    }

    @FXML
    private void handleCheckTerms() {
        // TODO: Implement terms
    }

    @FXML
    private void handleCloseApp() {
        stopConnectionCheck();
        sseManager.closeConnection();

        ((Stage) closeApp.getScene().getWindow()).close();
        System.exit(0);
    }

    private void startConnectionMonitoring() {
        connectionCheckService = Executors.newSingleThreadScheduledExecutor();

        connectionCheckService.schedule(() -> {
            connectionCheckService.scheduleAtFixedRate(() -> {
                CheckClientConnection.checkConnectionAsync(AppConfig.getCHECK_CONNECTION_URL())
                        .thenAccept(isConnected -> {
                            if (!isConnected) {
                                Platform.runLater(() -> {
                                    if (dragArea != null && dragArea.getScene() != null) {
                                        handleLostConnection();
                                    } else {Logger.error("Cant handle lost connection - UI not ready"); }
                                });
                            }
                        });
            }, 0, 5, TimeUnit.SECONDS);
        }, 1, TimeUnit.SECONDS);
    }

    public void stopConnectionCheck() {
        if (connectionCheckService != null && !connectionCheckService.isShutdown()) {
            connectionCheckService.shutdown();
        }
    }

    private void handleLostConnection() {
        stopConnectionCheck();
        sseManager.closeConnection();

        try {
            if (dragArea == null || dragArea.getScene() == null) {
                Logger.error("UI components are not ready when handling lost connection");
                ErrorScreens.showErrorScreen("Cant handle lost connection - UI not ready");
                return;
            }

            Stage stage = (Stage) dragArea.getScene().getWindow();
            if (stage == null) {
                Logger.error("Cant get stage reference when handling lost connection");
                ErrorScreens.showErrorScreen("Cant handle lost connection - Stage reference unavailable");
                return;
            }

            LoadingScreenController.showLoadScreen(stage, "Lost connection with server", CURRENT_PAGE);

            ScheduledExecutorService reconnectService = Executors.newSingleThreadScheduledExecutor();
            reconnectService.scheduleAtFixedRate(() -> {
                CompletableFuture<Boolean> isConnected = CheckClientConnection.checkConnectionAsync(AppConfig.getCHECK_CONNECTION_URL());
                isConnected.thenAccept(connected -> {
                    if (connected) {
                        Platform.runLater(() -> {
                            try {
                                reconnectService.shutdown();
                            } catch (Exception e) {
                                Logger.error("Error restoring connection: " + e.getMessage());
                            }
                        });
                    }
                });
            }, 0, 5, TimeUnit.SECONDS);

        } catch (Exception e) {
            Logger.error("Error in handleLostConnection: " + e.getMessage());
            ErrorScreens.showErrorScreen("Error while handling lost connection");
        }
    }

    @Override
    public void updateUILanguage(ResourceBundle bundle) {
        try {
            btnSignin.setText(bundle.getString("signin.button"));
            btnSignup.setText(bundle.getString("signup.button"));
            btnForgotPass.setText(bundle.getString("forgotpass.button"));
            fieldUsername.setPromptText(bundle.getString("username.prompt"));
            fieldPassword.setPromptText(bundle.getString("password.prompt"));
            btnGoogle.setText(bundle.getString("google.button"));
            btnFacebook.setText(bundle.getString("facebook.button"));
            downlApp.setText(bundle.getString("download.app"));
            languageComboBox.setPromptText(bundle.getString("language.combobox"));
            madeby.setText(bundle.getString("madeby"));
            dontHaveAcc.setText(bundle.getString("dont.have.account"));
            or.setText(bundle.getString("or"));
            remember_checkBox.setText(bundle.getString("remember.checkbox"));
            checkTerms.setText(bundle.getString("check.terms"));
        } catch (Exception e) {
            Logger.error("UI update failed:" + e.getMessage());
        }
    }

    public static void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void showInfoDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}