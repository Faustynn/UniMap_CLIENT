package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.RegistrationService;
import org.main.unimap_pc.client.services.SecurityService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;


public class SignUpController implements LanguageSupport {
    private static final int ERROR_LOGIN_EXISTS = 303;
    private static final int ERROR_EMAIL_EXISTS = 304;
    private static final int ERROR_USERNAME_EXISTS = 305;

    @FXML private FontAwesomeIcon closeApp;
    @FXML private AnchorPane dragArea;
    @FXML private Label userRegistr;
    @FXML private Label infoMess;
    @FXML private TextField fieldUsername;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldLogin;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldControlPassword;
    @FXML private Button btnRegistr;
    @FXML private Label signIn_text;
    @FXML private Label have_acc_text;

    @Getter @Setter
    private boolean registrationInProgress = false;

    private final SecurityService securityService = new SecurityService();
    private final WindowDragHandler windowDragHandler = new WindowDragHandler();


    @FXML
    private void initialize() {
        initLanguageSupport();
        windowDragHandler.setupWindowDragging(dragArea);
    }


    private void initLanguageSupport() {
        String lang = PreferenceServise.get("LANGUAGE").toString();
        LanguageManager.getInstance().registerController(this);
        LanguageManager.changeLanguage(lang);
        updateUILanguage(LanguageManager.getCurrentBundle());
    }


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
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleRegisterBtn() {
        if (registrationInProgress) {
            return;
        }

        // Validate user input
        ValidationResult validationResult = validateUserInput();
        if (!validationResult.isValid()) {
            infoMess.setText(validationResult.getErrorMessage());
            return;
        }

        // Start registration process
        startRegistration();
    }


    private ValidationResult validateUserInput() {
        String login = fieldLogin.getText().trim();
        String email = fieldEmail.getText().trim();
        String username = fieldUsername.getText().trim();
        String password = fieldPassword.getText().trim();
        String confirmPassword = fieldControlPassword.getText().trim();

        // Validate login
        if (login.isEmpty()) {
            return new ValidationResult(false, "Please enter your login!");
        } else if (login.length() < 3 || login.length() > 20) {
            return new ValidationResult(false, "Login must be at least 3-20 characters!");
        }

        // Validate email
        if (email.isEmpty()) {
            return new ValidationResult(false, "Please enter your email!");
        } else if (!securityService.checkEmail(email)) {
            return new ValidationResult(false, "Please write correct email!");
        }

        // Validate username
        if (username.isEmpty()) {
            return new ValidationResult(false, "Please enter your username!");
        } else if (!securityService.checkNames(username)) {
            return new ValidationResult(false, "Username must be 2-32 characters and contain only letters and numbers!");
        }

        // Validate password
        if (password.isEmpty()) {
            return new ValidationResult(false, "Please enter your password!");
        } else if (!securityService.checkPassword(password)) {
            return new ValidationResult(false, "Password must be at least 10 characters long, contain at least one letter and one digit!");
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            return new ValidationResult(false, "Please confirm your password!");
        } else if (!confirmPassword.equals(password)) {
            return new ValidationResult(false, "Passwords do not match!");
        }

        return new ValidationResult(true, "");
    }


    private void startRegistration() {
        String username = fieldUsername.getText().trim();
        String email = fieldEmail.getText().trim();
        String login = fieldLogin.getText().trim();
        String password = fieldPassword.getText().trim();

        infoMess.setText("Registration in progress...");
        registrationInProgress = true;

        AtomicInteger errorCode = new AtomicInteger();

        RegistrationService.registration(username, password, email, login, errorCode)
                .thenAccept(isSuccessful -> Platform.runLater(() -> handleRegistrationResult(isSuccessful, errorCode.get())))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        infoMess.setText("Registration request failed. Please try again later!");
                        registrationInProgress = false;
                    });
                    return null;
                });
    }


    private void handleRegistrationResult(boolean isSuccessful, int errorCode) {
        registrationInProgress = false;

        if (isSuccessful) {
            handleSuccessfulRegistration();
        } else {
            handleFailedRegistration(errorCode);
        }
    }


    private void handleSuccessfulRegistration() {
        infoMess.setText("Registration successful!");
        clearFields();
    }


    private void handleFailedRegistration(int errorCode) {
        switch (errorCode) {
            case ERROR_LOGIN_EXISTS -> infoMess.setText("User with this login already exists!");
            case ERROR_EMAIL_EXISTS -> infoMess.setText("User with this email already exists!");
            case ERROR_USERNAME_EXISTS -> infoMess.setText("User with this username already exists!");
            default -> infoMess.setText("Error during registration. Please try again later!");
        }
    }

    private void clearFields() {
        fieldUsername.clear();
        fieldEmail.clear();
        fieldLogin.clear();
        fieldPassword.clear();
        fieldControlPassword.clear();
    }


    @FXML
    private void move_to_sign_in() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }


    @Getter
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
    }
}