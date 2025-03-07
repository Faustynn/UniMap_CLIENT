package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.RegistrationService;
import org.main.unimap_pc.client.services.SecurityService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class SignUpController implements LanguageSupport {
    public Label signIn_text;
    public Label have_acc_text;
    @FXML
    private FontAwesomeIcon closeApp;

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

    private final SecurityService securityService = new SecurityService();


    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        infoMess.setText(languageBundle.getString("info.message"));
        btnRegistr.setText(languageBundle.getString("register.button"));
        fieldUsername.setPromptText(languageBundle.getString("username.res.prompt"));
        fieldEmail.setPromptText(languageBundle.getString("email.prompt"));
        fieldLogin.setPromptText(languageBundle.getString("login.prompt"));
        fieldPassword.setPromptText(languageBundle.getString("password.prompt"));
        fieldControlPassword.setPromptText(languageBundle.getString("confirm.password.prompt"));
        userRegistr.setText(languageBundle.getString("user.reg"));

        signIn_text.setText(languageBundle.getString("sign.in.text"));
        have_acc_text.setText(languageBundle.getString("have.acc.text"));
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
    private TextField fieldUsername;
    @FXML
    private TextField fieldEmail;
    @FXML
    private TextField fieldLogin;
    @FXML
    private PasswordField fieldPassword;
    @FXML
    private PasswordField fieldControlPassword;
    @FXML
    private Button btnRegistr;


    @FXML
    private void handleRegisterBtn(){
        String username = fieldUsername.getText().trim();
        String email = fieldEmail.getText().trim();
        String login = fieldLogin.getText().trim();
        String password = fieldPassword.getText().trim();
        String confirmPassword = fieldControlPassword.getText().trim();


        if (login.isEmpty()) {
            infoMess.setText("Please enter your login!");
            return;
        } else if (login.length() < 3 || login.length() > 20) {
            infoMess.setText("Login must be at least 3-20 characters!");
            return;
        }

        if (email.isEmpty()) {
            infoMess.setText("Please enter your email!");
            return;
        } else if (!securityService.checkEmail(email)) {
            infoMess.setText("Please write correct email!");
            return;
        }

        if (username.isEmpty()) {
            infoMess.setText("Please enter your username!");
            return;
        } else if (!securityService.checkNames(username)) {
            infoMess.setText("Username must be 2-32 characters and contain only letters and numbers!");
            return;
        }

        if (password.isEmpty()) {
            infoMess.setText("Please enter your password!");
            return;
        } else if (!securityService.checkPassword(password)) {
            infoMess.setText("Password must be at least 10 characters long, contain at least one letter and one digit!");
            return;
        }

        if (confirmPassword.isEmpty()) {
            infoMess.setText("Please confirm your password!");
            return;
        } else if (!confirmPassword.equals(password)) {
            infoMess.setText("Passwords do not match!");
            return;
        }

        infoMess.setText("Registration in progress...");

        AtomicInteger code = new AtomicInteger();

        RegistrationService.registration(username, password,email,login,code).thenAccept(isLoginSuccessful -> Platform.runLater(() -> {
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
        })).exceptionally(ex -> {
            Platform.runLater(() -> infoMess.setText("Registration request failed. Please try again later!"));
            return null;
        });
    }

    @FXML
    private void move_to_sign_in() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }
}