package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.AuthService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.ErrorScreens;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

public class LogInController implements LanguageSupport {
    public Label downlApp;
    public MFXButton btnFacebook;
    public MFXButton btnGoogle;
    @FXML
    private CheckBox remember_checkBox,checkTerms;
    @FXML
    private Label closeApp;

    @FXML
    private AnchorPane dragArea;

    @FXML
    private Label madeby;

    @FXML
    private Label dontHaveAcc;

    @FXML
    private Label or;

    @FXML
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    private double xOffset = 0;
    private double yOffset = 0;




    @FXML
    private void initialize() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        PreferenceServise.put(AppConfig.getLANGUAGE_KEY(), "EN");

        loadCurrentLanguage();
        dragArea.setOnMousePressed(this::handleMousePressed);
        dragArea.setOnMouseDragged(this::handleMouseDragged);
        LanguageManager.getInstance().registerController(this);
    }
    private void loadCurrentLanguage() {
        String selectedLanguage = PreferenceServise.get(AppConfig.getLANGUAGE_KEY()).toString();
        languageComboBox.setValue(selectedLanguage);

        // listener for lang. editing
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                showErrorDialog("Error changing language: " + e.getMessage());
                loadCurrentLanguage();
            }
        });
    }
    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        try {
            btnSignin.setText(languageBundle.getString("signin.button"));
            btnSignup.setText(languageBundle.getString("signup.button"));
            btnForgotPass.setText(languageBundle.getString("forgotpass.button"));
            fieldUsername.setPromptText(languageBundle.getString("username.prompt"));
            fieldPassword.setPromptText(languageBundle.getString("password.prompt"));
            btnGoogle.setText(languageBundle.getString("google.button"));
            btnFacebook.setText(languageBundle.getString("facebook.button"));
            downlApp.setText(languageBundle.getString("download.app"));
            languageComboBox.setText(languageBundle.getString("language.combobox"));
            closeApp.setText(languageBundle.getString("close"));
            madeby.setText(languageBundle.getString("madeby"));
            dontHaveAcc.setText(languageBundle.getString("dont.have.account"));
            or.setText(languageBundle.getString("or"));
        } catch (Exception e) {
            System.err.println("Error updating UI language: " + e.getMessage());
        }
    }
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
    private MFXTextField fieldUsername;

    @FXML
    private MFXPasswordField fieldPassword;

    @FXML
    private Label btnForgotPass;

    @FXML
    private MFXButton btnSignin;
    @FXML
    private MFXButton btnSignup;


    @FXML
    private Label infoMess;

    @FXML
    private MFXComboBox<String> languageComboBox;

    // If click into downlApp label open github page
    @FXML
    private void handleDownlApp() {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(AppConfig.getGithubPage()));
        } catch (IOException e) {
            showErrorDialog("Error opening the GitHub page: " + e.getMessage());
        }
    }

    public static void showErrorDialog(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);

        alert.showAndWait();
    }

    // Modal Window logic
    @FXML
    private void openModalWindow(String fxmlPath, String windowTitle, String errorMessage) {
        try {
            Stage parentStage = (Stage) (windowTitle.equals("Forgot Password") ? btnForgotPass : btnSignup).getScene().getWindow();

            // Проверяем, существует ли ресурс
            if (getClass().getResource(fxmlPath) == null) {
                showErrorDialog("Resource not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            // Устанавливаем ресурсный бандл, если он доступен
            if (LanguageManager.getCurrentBundle() != null) {
                loader.setResources(LanguageManager.getCurrentBundle());
            }

            try {
                AnchorPane modalPane = loader.load();

                Scene modalScene = new Scene(modalPane);
                Stage modalStage = new Stage();

                // Configure modal stage
                modalStage.initStyle(StageStyle.TRANSPARENT);
                modalStage.initModality(Modality.WINDOW_MODAL);
                modalStage.initOwner(parentStage);
                modalStage.setTitle(windowTitle);

                modalStage.setScene(modalScene);

                // Create overlay for parent stage
                StackPane overlay = createOverlay(parentStage);

                // Add overlay to parent scene
                Scene parentScene = parentStage.getScene();
                AnchorPane parentRoot = (AnchorPane) parentScene.getRoot();
                parentRoot.getChildren().add(overlay);

                // Remove overlay when modal stage is closed
                modalStage.setOnHidden(event -> parentRoot.getChildren().remove(overlay));

                // Show and wait
                modalStage.showAndWait();

            } catch (IOException e) {
                System.err.println("Failed to load FXML from path: " + fxmlPath);
                e.printStackTrace();
                showErrorDialog(errorMessage + ": " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in openModalWindow");
            e.printStackTrace();
            showErrorDialog(errorMessage + ": " + e.getMessage());
        }
    }

    // Helper method to create overlay
    private StackPane createOverlay(Stage parentStage) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setPrefSize(parentStage.getWidth(), parentStage.getHeight());

        // Block main screen and play system sound on click
        overlay.setOnMouseClicked(event -> Toolkit.getDefaultToolkit().beep());

        return overlay;
    }

    // Updated method calls
    @FXML
    private void handleForgotPass() {
        openModalWindow(
                AppConfig.getForgotPassPagePath(),
                "Forgot Password",
                "Error loading the forgot password window"
        );
    }

    @FXML
    private void handleSignUp() {
        try {
            openModalWindow(
                    AppConfig.getSignupPagePath(),
                    "Sign Up",
                    "Error loading the sign up window"
            );
        } catch (Exception e) {
            System.err.println("Error loading SignUpPage.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignIn() {
        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            infoMess.setText("Please enter your username and password!");
            return;
        }
        if (username.length() < 3 || password.length() < 3) {
            infoMess.setText("Username and password must be at least 3 characters long!");
            return;
        }

        AuthService.login(username, password).thenAccept(isLoginSuccessful -> Platform.runLater(() -> {
            if (isLoginSuccessful) {
                try {
                    Stage currentStage = (Stage) btnSignin.getScene().getWindow();
                    Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getMainPagePath())));
                    Scene mainScene = new Scene(root);
                    currentStage.setScene(mainScene);
                    currentStage.setFullScreen(true);
                    currentStage.show();

                } catch (IOException e) {
                    System.err.println("Failed to load main page: " + e.getMessage());
                    showErrorDialog("Error loading the application. Please try again later.");
                }
            } else {
                infoMess.setText("Invalid username or password!");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> infoMess.setText("Login request failed. Please try again later."));
            return null;
        });
    }



    @FXML
    private void handleSignByGoogle() {
        try {
            String authUrl = AppConfig.getOauth2Google();

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            }
        } catch (Exception e) {
            ErrorScreens.showErrorScreen("Failed to open authentication page");
        }
    }

    @FXML
    private void handleSignByFacebook() {
        try {
            String authUrl = AppConfig.getOauth2Facebook();

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            }
        } catch (Exception e) {
            ErrorScreens.showErrorScreen("Failed to open authentication page");
        }
    }

    @FXML
    private void handleCheckBox() {
        if (remember_checkBox.isSelected()){
                PreferenceServise.put("REMEMBER", true);
        } else {
            PreferenceServise.put("REMEMBER", false);
        }
    }

    @FXML
    private void handleCheckTerms() {
        if (checkTerms.isSelected()) {
            // TODO: terms
        } else {
        }
    }
}