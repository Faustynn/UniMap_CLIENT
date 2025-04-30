package org.main.unimap_pc.client.controllers;

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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;
import static org.main.unimap_pc.client.controllers.LogInController.showInfoDialog;


public class SettingsPageController implements LanguageSupport {
    @FXML private MFXButton btn_homepage;
    @FXML private MFXButton btn_profilepage;
    @FXML private MFXButton btn_subjectpage;
    @FXML private MFXButton btn_teacherspage;
    @FXML private MFXButton btn_settingspage;
    @FXML private MFXButton logoutbtn;

    @FXML private AnchorPane dragArea;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ImageView navi_avatar;
    @FXML private Label navi_username_text;
    @FXML private Label navi_login_text;
    @FXML private Button btnStartDeleteAcc;
    @FXML private Button btnStartDeletComments;
    @FXML private Label privacy_text;
    @FXML private Label settings_text;
    @FXML private Label pair_mobile_text;
    @FXML private ImageView qr_image;
    @FXML private Label political_terms_text;
    @FXML private Label sourse_code_text;
    @FXML private Label support_text;

    @Getter @Setter
    private String defaultLanguage;

    private final WindowDragHandler windowDragHandler = new WindowDragHandler();


    @FXML
    private void initialize() {
        try {
            initUserInfo();
            initWindowDragging();
            initLanguageSupport();
        } catch (Exception e) {
            Logger.error("Error during initializing settings page: " + e.getMessage());
        }
    }


    private void initUserInfo() {
        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            navi_username_text.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
        }
    }


    private void initWindowDragging() {
        windowDragHandler.setupWindowDragging(dragArea);
    }


    private void initLanguageSupport() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        defaultLanguage = PreferenceServise.get("LANGUAGE").toString();
        loadCurrentLanguage();

        LanguageManager.changeLanguage(defaultLanguage);
        LanguageManager.getInstance().registerController(this);
        updateUILanguage(LanguageManager.getCurrentBundle());
    }


    private void loadCurrentLanguage() {
        languageComboBox.setValue(defaultLanguage);
        languageComboBox.setOnAction(event -> handleLanguageChange());
    }


    private void handleLanguageChange() {
        try {
            String newLanguage = languageComboBox.getValue();
            String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
            LanguageManager.changeLanguage(languageCode);
            PreferenceServise.put("LANGUAGE", languageCode);
            updateUILanguage(LanguageManager.getCurrentBundle());
        } catch (Exception e) {
            Logger.error("Error in handleLanguageChange(): " + e.getMessage());
        }
    }


    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        // Navigation buttons
        logoutbtn.setText(languageBundle.getString("logout"));
        btn_homepage.setText(languageBundle.getString("homepage"));
        btn_profilepage.setText(languageBundle.getString("profilepage"));
        btn_subjectpage.setText(languageBundle.getString("subjectpage"));
        btn_teacherspage.setText(languageBundle.getString("teacherspage"));
        btn_settingspage.setText(languageBundle.getString("settingspage"));

        // Settings page elements
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
    private void handleHomePageClick() {
        navigateToPage(AppConfig.getMAIN_PAGE_PATH());
    }

    @FXML
    private void handleProfilePageClick() {
        navigateToPage(AppConfig.getPROFILE_PAGE_PATH());
    }

    @FXML
    private void handleSubjectPageClick() {
        navigateToPage(AppConfig.getSUBJECTS_PAGE_PATH());
    }

    @FXML
    private void handleTeachersPageClick() {
        navigateToPage(AppConfig.getTEACHERS_PAGE_PATH());
    }

    @FXML
    private void handleSettingsPageClick() {
        navigateToPage(AppConfig.getSETTINGS_PAGE_PATH());
    }


    private void navigateToPage(String pagePath) {
        try {
            Stage currentStage = (Stage) btn_homepage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(pagePath)));
            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }


    @FXML
    private void handleLogout() {
        try {
            clearUserData();
            navigateToLoginPage();
        } catch (IOException e) {
            Logger.error("Failed to logout: " + e.getMessage());
            showErrorDialog("Error logging out. Please try again later.");
        }
    }


    private void clearUserData() {
        PreferenceServise.deletePreferences();
        PreferenceServise.put("REMEMBER", false);
        CacheService.clearCache();
    }


    private void navigateToLoginPage() throws IOException {
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLOGIN_PAGE_PATH())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }

    @FXML
    private void open_terms() {
        openExternalUrl("https://github.com/Faustynn/UniMap_CLIENT");
    }

    @FXML
    private void open_source_code() {
        openExternalUrl("https://github.com/UniMapSTU");
    }

    @FXML
    private void open_support() {
        openExternalUrl("https://bank.gov.ua/ua/about/support-the-armed-forces");
    }


    private void openExternalUrl(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            Logger.error("Failed to open URL: " + e.getMessage());
            showErrorDialog("Failed to open URL. Please try manually.");
        }
    }


    @FXML
    private void handleStartDeleteAcc() {
        String userId = UserService.getInstance().getCurrentUser().getId();
        deleteUserAccount(userId);
    }


    private void deleteUserAccount(String userId) {
        CompletableFuture<Boolean> deleteResult = UserService.delete_user(userId);

        deleteResult.thenAccept(success -> {
            if (success) {
                Platform.runLater(this::handleSuccessfulAccountDeletion);
            } else {
                Platform.runLater(this::handleFailedAccountDeletion);
            }
        });
    }


    private void handleSuccessfulAccountDeletion() {
        clearUserData();
        try {
            navigateToLoginPage();
        } catch (IOException e) {
            Logger.error("Failed to navigate to login page after account deletion: " + e.getMessage());
            showErrorDialog("Account deleted, but error navigating to login page.");
        }
    }


    private void handleFailedAccountDeletion() {
        Logger.warning("Failed to delete account. Please try again later.");
        showErrorDialog("Failed to delete account. Please try again later.");
    }


    @FXML
    private void handleStartDeleteComments() {
        String userId = UserService.getInstance().getCurrentUser().getId();
        deleteUserComments(userId);
    }


    private void deleteUserComments(String userId) {
        CompletableFuture<Boolean> deleteResult = UserService.delete_all_user_comments(userId);

        deleteResult.thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showInfoDialog("Comments deleted successfully.");
                } else {
                    Logger.warning("Failed to delete comments. Please try again later.");
                    showErrorDialog("Failed to delete comments. Please try again later.");
                }
            });
        });
    }
}