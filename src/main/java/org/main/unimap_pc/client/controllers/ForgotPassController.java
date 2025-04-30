package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.EmailService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.SecurityService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.WindowDragHandler;
import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

import java.io.IOException;
import java.util.ResourceBundle;

public class ForgotPassController implements LanguageSupport {
    @FXML private FontAwesomeIcon closeApp;
    @FXML private AnchorPane dragArea;
    @FXML private Button btnSendMail;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldCode;
    @FXML private Label infoMess;
    @FXML private Label infoMess2;
    @FXML private Label reset_text;

    @Setter private String email;
    private final SecurityService securityService = new SecurityService();
    private final WindowDragHandler windowDragHandler = new WindowDragHandler();

    @FXML
    private void initialize() {
        String lang = PreferenceServise.get("LANGUAGE").toString();
        LanguageManager.getInstance().registerController(this);
        LanguageManager.changeLanguage(lang);
        updateUILanguage(LanguageManager.getCurrentBundle());

        windowDragHandler.setupWindowDragging(dragArea);
    }

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        btnSendMail.setText(languageBundle.getString("send"));

        if (fieldEmail != null) {
            fieldEmail.setPromptText(languageBundle.getString("email"));
        }

        if (fieldCode != null) {
            fieldCode.setPromptText(languageBundle.getString("code"));
        } else if (reset_text != null) {
            reset_text.setText(languageBundle.getString("reset.text"));
        }
    }

    @FXML
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void SendMailbtn() {
        String emailInput = fieldEmail.getText();

        if (emailInput.isEmpty()) {
            setErrorMessage("Please enter your email address");
            return;
        }

        if (!securityService.checkEmail(emailInput)) {
            setErrorMessage("Invalid email address");
            return;
        }

        String url = AppConfig.getFIND_USER_BY_EMAIL_URL() + emailInput;
        EmailService.checkEmail(url, emailInput).thenAccept(result ->
                Platform.runLater(() -> handleEmailCheckResult(result, emailInput))
        );
    }

    private void setErrorMessage(String message) {
        infoMess.setText(message);
    }

    private void handleEmailCheckResult(boolean userExists, String email) {
        if (!userExists) {
            setErrorMessage("User with this email doesn't exist!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConfig.getFORGOT_PASS_PAGE_PATH2()));
            Parent root = loader.load();
            ForgotPassController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) fieldEmail.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            setErrorMessage("Error #418 :), please try again or contact support");
            Logger.error("Error #418: Failed to load Forgot Password page. " + e.getMessage());
        }
    }

    @FXML
    public void ConfirmMailbtn() {
        String codeInput = fieldCode.getText();

        if (codeInput.isEmpty()) {
            infoMess2.setText("Please enter your code from email!");
            return;
        }

        if (!securityService.checkEmailCode(codeInput)) {
            infoMess2.setText("The code must be 6 numbers!");
            return;
        }

        String confirmCodeUrl = AppConfig.getCONFIRM_CODE_TO_EMAIL();
        EmailService.checkCode(confirmCodeUrl, codeInput, email).thenAccept(result ->
                Platform.runLater(() -> handleCodeVerificationResult(result))
        );
    }

    private void handleCodeVerificationResult(boolean isValidCode) {
        if (!isValidCode) {
            infoMess2.setText("Invalid code!");
            return;
        }

        infoMess2.setText("Please enter your new password!");
        fieldCode.clear();
        setupPasswordChangeListener();
    }

    private void setupPasswordChangeListener() {
        fieldCode.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!securityService.checkPassword(newValue)) {
                infoMess2.setText("Password must be at least 10 characters long, contain at least one letter and one digit!");
                return;
            }

            try {
                fieldCode.setDisable(true);
                String changePasswordUrl = AppConfig.getCHANGE_PASSWORD();

                EmailService.updatePassword(changePasswordUrl, newValue, email)
                        .thenAccept(result -> Platform.runLater(() -> {
                            if (!result) {
                                infoMess2.setText("Failed to change password. Please try again.");
                                fieldCode.setDisable(false);
                            } else {
                                infoMess2.setText("Password changed successfully!");
                                Stage stage = (Stage) fieldCode.getScene().getWindow();
                                stage.close();
                            }
                        }));
            } catch (Exception e) {
                Logger.error("Encryption error: " + e.getMessage());
                showErrorDialog("Encryption error: " + e.getMessage());
            }
        });
    }
}