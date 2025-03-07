package org.main.unimap_pc.client.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.JWTService;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;
import static org.main.unimap_pc.client.controllers.LogInController.showInfoDialog;

public class SettingsPageController implements LanguageSupport {

    public ImageView navi_avatar;
    public Label navi_username_text;
    public Label navi_login_text;
    public Button btnStartDeleteAcc;
    public Button btnStartDeletComments;
    public Label privacy_text;
    public Label settings_text;
    public Label pair_mobile_text;
    public ImageView qr_image;
    public Label political_terms_text;
    public Label sourse_code_text;
    public Label support_text;
    @FXML
    private AnchorPane dragArea;
    private double xOffset = 0;
    private double yOffset = 0;

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
    private ComboBox<String> languageComboBox;
    private String defLang;


    @FXML
    private void initialize() {
        try {
            UserModel user = UserService.getInstance().getCurrentUser();
            if (user != null) {
                UserService.getInstance().setCurrentUser(user);
                navi_username_text.setText(user.getUsername());
                navi_login_text.setText(user.getLogin());
                navi_avatar.setImage(AppConfig.getAvatar(user.getAvatar()));
            }

            dragArea.setOnMousePressed(this::handleMousePressed);
            dragArea.setOnMouseDragged(this::handleMouseDragged);

            Scene scene = dragArea.getScene();
            if (scene != null) {
                scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                    // TODO:Resize logic
                });
                scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                    // TODO:Resize logic
                });
            }

            languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
            defLang = PreferenceServise.get("LANGUAGE").toString();
            loadCurrentLanguage();
            LanguageManager.changeLanguage(defLang);
            LanguageManager.getInstance().registerController(this);
            updateUILanguage(LanguageManager.getCurrentBundle());
        } catch (Exception e) {
            Logger.error("Error during initializing settings page" + e.getMessage());
        }
    }

    private void loadCurrentLanguage() {
        languageComboBox.setValue(defLang);
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                PreferenceServise.put("LANGUAGE", languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                Logger.error("Error in loadCurrentLanguage(): " + e.getMessage());
            }
        });
    }

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        logoutbtn.setText(languageBundle.getString("logout"));

        btn_homepage.setText(languageBundle.getString("homepage"));
        btn_profilepage.setText(languageBundle.getString("profilepage"));
        btn_subjectpage.setText(languageBundle.getString("subjectpage"));
        btn_teacherspage.setText(languageBundle.getString("teacherspage"));
        btn_settingspage.setText(languageBundle.getString("settingspage"));

        settings_text.setText(languageBundle.getString("settings_text"));
        privacy_text.setText(languageBundle.getString("privacy_text"));
        pair_mobile_text.setText(languageBundle.getString("pair_mobile_text"));
        political_terms_text.setText(languageBundle.getString("political_terms_text"));
        sourse_code_text.setText(languageBundle.getString("sourse_code_text"));
        support_text.setText(languageBundle.getString("support_text"));
        btnStartDeleteAcc.setText(languageBundle.getString("btnStartDeleteAcc"));
        btnStartDeletComments.setText(languageBundle.getString("btnStartDeletComments"));
    }

    @FXML
    private MFXButton btn_homepage;
    @FXML
    private MFXButton btn_profilepage;
    @FXML
    private MFXButton btn_subjectpage;
    @FXML
    private MFXButton btn_teacherspage;
    @FXML
    private MFXButton btn_settingspage;

    @FXML
    public void handleHomePageClick() {
        try {
            Stage currentStage = (Stage) btn_homepage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getMainPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }
    @FXML
    public void handleProfilePageClick() {
        try {
            Stage currentStage = (Stage) btn_profilepage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getProfilePagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }
    @FXML
    public void handleSubjectPageClick() {
        try {
            Stage currentStage = (Stage) btn_subjectpage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getSubjectsPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }
    @FXML
    public void handleTeachersPageClick() {
        try {
            Stage currentStage = (Stage) btn_teacherspage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getTeachersPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }
    @FXML
    public void handleSettingsPageClick() {
        try {
            Stage currentStage = (Stage) btn_settingspage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getSettingsPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private MFXButton logoutbtn;
    @FXML
    private void handleLogout() throws IOException {
        // Clear the user data
        PreferenceServise.deletePreferences();
        PreferenceServise.put("REMEMBER", false);
        CacheService.clearCache();


        // Change scene to login
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLoginPagePath())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }


    @FXML
    private void open_terms(){
        try {
            String url = "https://github.com/Faustynn/UniMap_CLIENT";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void open_source_code(){
        try {
            String url = "https://github.com/UniMapSTU";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void open_support(){
        try {
            String url = "https://bank.gov.ua/ua/about/support-the-armed-forces";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



@FXML
private void handleStartDeleteAcc() {
    System.out.println("Delete user");
    UserService.delete_user(UserService.getInstance().getCurrentUser().getId())
        .thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    PreferenceServise.deletePreferences();
                    CacheService.clearCache();

                    // Logout
                    try {
                        Stage stage = (Stage) btnStartDeleteAcc.getScene().getWindow();
                        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLoginPagePath())));
                        Scene mainScene = new Scene(root);
                        stage.setScene(mainScene);
                        stage.show();
                    } catch (IOException e) {
                        Logger.error("Failed to load login page: " + e.getMessage());
                        showErrorDialog("Error logging out. Please try again later.");
                    }
                });
            } else {
                Platform.runLater(() -> {
                    showErrorDialog("Failed to delete account. Please try again later.");
                });
            }
        });
}

@FXML
private void handleStartDeleteComments() {
    System.out.println("Delete comments");
    UserService.delete_all_user_comments(UserService.getInstance().getCurrentUser().getId())
            .thenAccept(success -> {
                if (success) {
                    Platform.runLater(() -> {
                        showInfoDialog("Comments deleted successfully.");
                    });
                } else {
                    Platform.runLater(() -> {
                        showErrorDialog("Failed to delete comments. Please try again later.");
                    });
                }
            });
}





}
