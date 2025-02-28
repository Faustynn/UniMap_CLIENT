package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.RegistrationService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class SignUpController implements LanguageSupport {
    @FXML
    private Label closeApp;

    @FXML
    private AnchorPane dragArea;

    @FXML
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }
    @FXML
    private Label userRegistr;

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        closeApp.setText(languageBundle.getString("close"));
        infoMess.setText(languageBundle.getString("info.message"));
        btnRegistr.setText(languageBundle.getString("register.button"));
        fieldUsername.setPromptText(languageBundle.getString("username.prompt"));
        fieldEmail.setPromptText(languageBundle.getString("email.prompt"));
        fieldLogin.setPromptText(languageBundle.getString("login.prompt"));
        fieldPassword.setPromptText(languageBundle.getString("password.prompt"));
        fieldControlPassword.setPromptText(languageBundle.getString("confirm.password.prompt"));
        userRegistr.setText(languageBundle.getString("user.reg"));
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
    private Label infoMess;
    @FXML
    private MFXTextField fieldUsername;
    @FXML
    private MFXTextField fieldEmail;
    @FXML
    private MFXTextField fieldLogin;
    @FXML
    private MFXPasswordField fieldPassword;
    @FXML
    private MFXPasswordField fieldControlPassword;
    @FXML
    private Button btnRegistr;


    @FXML
    private void handleRegisterBtn(){
        if (fieldLogin.getText().isEmpty()) {
            infoMess.setText("Please enter your login!");
            return;
        }else if(fieldLogin.getText().length() < 3 || fieldLogin.getText().length() > 20){
            infoMess.setText("Login must be at least 3-20 characters!");
            return;
        }

        if (fieldEmail.getText().isEmpty()) {
            infoMess.setText("Please enter your email!");
            return;
        }
        if (fieldEmail.getText().length() < 5 || !fieldEmail.getText().contains("@") || !fieldEmail.getText().contains(".")) {
            infoMess.setText("Please write correct email!");
            return;
        }
        if (fieldUsername.getText().isEmpty()) {
            infoMess.setText("Please enter your username!");
            return;
        }else if(fieldUsername.getText().length() < 3 || fieldUsername.getText().length() > 50){
            infoMess.setText("Username must be at least 3-50 characters!");
            return;
        }
        if (fieldPassword.getText().isEmpty()) {
            infoMess.setText("Please enter your password!");
            return;
        }else if (fieldPassword.getText().isEmpty()) {
            infoMess.setText("Write minimal 8 characters!");
            return;
        }else if(fieldPassword.getText().contains(" ")) {
            infoMess.setText("Password must contain at least one number or special character!");
            return;
        }
        if(fieldControlPassword.getText().isEmpty()){
            infoMess.setText("Please confirm your password!");
            return;
        } else if (!fieldControlPassword.getText().equals(fieldPassword.getText())) {
            infoMess.setText("Please check correctness of your password!");
            return;
        }
        infoMess.setText("Registration in progress...");


        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText().trim();
        String email = fieldEmail.getText().trim();
        String login = fieldLogin.getText().trim();
        AtomicInteger code = new AtomicInteger();

        RegistrationService.registration(username, password,email,login,code).thenAccept(isLoginSuccessful -> {
            Platform.runLater(() -> {
                if (isLoginSuccessful) {
                    infoMess.setText("Registration successful!");
                    fieldUsername.clear();
                    fieldEmail.clear();
                    fieldLogin.clear();
                    fieldPassword.clear();
                    fieldControlPassword.clear();
                } else if (code.get() == 303) {
                    infoMess.setText("User with this login already exists!");
                } else if (code.get() == 304) {
                    infoMess.setText("User with this email already exists!");
                } else if (code.get() == 305) {
                    infoMess.setText("User with this username already exists!");
                } else {
                    infoMess.setText("Error during registration. Please try again later!");
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> infoMess.setText("Registration request failed. Please try again later!"));
            return null;
        });
    }

}