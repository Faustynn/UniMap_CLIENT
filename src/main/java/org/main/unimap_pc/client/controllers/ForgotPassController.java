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
import javafx.scene.layout.AnchorPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.EmailService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.ResourceBundle;

import static org.main.unimap_pc.client.configs.AppConfig.getForgotPassPagePath2;
import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class ForgotPassController implements LanguageSupport {
    @FXML
    private FontAwesomeIcon closeApp;

    @FXML
    private AnchorPane dragArea;

    @FXML
    private Button btnSendMail;

    @FXML
    private Button btnConfirmMail;

    @FXML
    private TextField fieldEmail;

    @FXML
    private TextField fieldCode;

    @FXML
    private Label infoMess;

    @FXML
    private Label infoMess2;

    @Setter
    private String email;

    @FXML
    private Label reset_text;


    @FXML
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }


    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        btnSendMail.setText(languageBundle.getString("send"));
        if (fieldEmail == null) {
            fieldCode.setPromptText(languageBundle.getString("code"));
        }else {
            fieldEmail.setPromptText(languageBundle.getString("email"));
        }
        if (fieldCode != null) {
            fieldCode.setPromptText(languageBundle.getString("code"));
        }else {
            reset_text.setText(languageBundle.getString("reset.text"));
        }
    }

    @FXML
    private void initialize() {
        String lang = PreferenceServise.get("LANGUAGE").toString();
        LanguageManager.getInstance().registerController(this);

        LanguageManager.changeLanguage(lang);
        updateUILanguage(LanguageManager.getCurrentBundle());

        dragArea.setOnMousePressed(this::handleMousePressed);
        dragArea.setOnMouseDragged(this::handleMouseDragged);
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
    private void SendMailbtn() {
        if (fieldEmail.getText().isEmpty()) {
            infoMess.setText("Please enter your email address");
        } else if (fieldEmail.getText().length() < 5 || !fieldEmail.getText().contains("@") || !fieldEmail.getText().contains(".")) {
            infoMess.setText("Invalid email address");
            // fieldEmail.clear();
        } else {
            String url = AppConfig.getFindUserByEmailUrl() + fieldEmail.getText();
            System.out.println(url);
            EmailService.checkEmail(url, fieldEmail.getText()).thenAccept(result -> Platform.runLater(() -> {
                if (!result) {
                    infoMess.setText("User with this email dont exist!");
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(getForgotPassPagePath2()));
                        Parent root = loader.load();
                        ForgotPassController controller = loader.getController();
                        controller.setEmail(fieldEmail.getText());
                        Stage stage = (Stage) fieldEmail.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                    } catch (IOException e) {
                        infoMess.setText("Error #418 :), please try again or contact support");
                        e.printStackTrace();
                    }
                }
            }));
        }
    }


    @FXML
    public void ConfirmMailbtn() {
        String url = AppConfig.getConfirmCodeToEmail();
        String url2 = AppConfig.getChangePassword();

        if (fieldCode.getText().isEmpty()) {
            infoMess2.setText("Please enter your code from email!");
        } else if (fieldCode.getText().length() != 6) {
            infoMess2.setText("The code must be 6 numbers!");
        } else {
            EmailService.checkCode(url, fieldCode.getText(), email).thenAccept(result -> Platform.runLater(() -> {
                if (!result) {
                    infoMess2.setText("Invalid code!");
                } else {
                    infoMess2.setText("Please enter your new password!");
                    fieldCode.clear();

                    fieldCode.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.length() < 6) {
                            infoMess2.setText("Password must be at least 6 characters!");
                            return;
                        }

                        try {
                            fieldCode.setDisable(true);

                            EmailService.updatepassword(url2, newValue, email).thenAccept(result2 -> Platform.runLater(() -> {
                                if (!result2) {
                                    infoMess2.setText("Failed to change password. Please try again.");
                                    fieldCode.setDisable(false);
                                } else {
                                    infoMess2.setText("Password changed successfully!");
                                    Stage stage = (Stage) fieldCode.getScene().getWindow();
                                    stage.close();

                                }
                            }));
                        } catch (Exception e) {
                            showErrorDialog("Encryption error: " + e.getMessage());
                        }
                    });
                }
            }));
        }
    }
}