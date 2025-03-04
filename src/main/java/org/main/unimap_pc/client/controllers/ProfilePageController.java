package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.PasswordChangeRequest;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper; // For JSON serialization
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField; // Import PasswordField

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.ResourceBundle;
import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class ProfilePageController {


    @FXML
    public MFXButton change_pass_btn;
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label surnameLabel;
    @FXML
    private Label avatarLabel;

    @FXML
    private PasswordField newPasswordField; // Use PasswordField for secure password input
    @FXML
    private HBox passwordContainer; // Container for password input elements

    @FXML
    private void initialize() {
        nameLabel.setText("Username: " + UserService.getInstance().getCurrentUser().getUsername());
        surnameLabel.setText("Surname: " + UserService.getInstance().getCurrentUser().getLogin());
        emailLabel.setText("Email: " + UserService.getInstance().getCurrentUser().getEmail());
        avatarLabel.setText("Avatar num: " + UserService.getInstance().getCurrentUser().getAvatar());

        Label label1 = new Label("New Password:");
        newPasswordField = new PasswordField(); // Initialize PasswordField

        passwordContainer.getChildren().addAll(label1, newPasswordField); //Add to container
        passwordContainer.setSpacing(10);
    }

    public void updateUILanguage(ResourceBundle languageBundle) {
    }

    @FXML
    private void handleChangePass() {
        try {
            String newPassword = newPasswordField.getText();
            String email = UserService.getInstance().getCurrentUser().getEmail();

            if (newPassword == null || newPassword.isEmpty()) {
                showErrorDialog("Please enter a new password.");
                return;
            }

            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setEmail(email);
            request.setNewPassword(newPassword);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(request);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getChangePasswordUrl())) // Replace with your API endpoint
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showErrorDialog("Password changed successfully.");
                newPasswordField.clear(); // Clear the password field after successful change
            } else if (response.statusCode() == 404) {
                showErrorDialog("User not found.");
            } else {
                showErrorDialog("Failed to change password: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("An error occurred while changing the password.");
        }
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
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
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
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
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
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
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
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
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
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }


    @FXML
    private MFXButton logoutbtn;
    @FXML
    private void handleLogout() throws IOException {
        // Clear the user data
        PreferenceServise.remove("ACCESS_TOKEN");
        PreferenceServise.remove("REFRESH_TOKEN");
        PreferenceServise.remove("USER_DATA");
        CacheService.remove("SUBJECTS");
        CacheService.remove("TEACHERS");


        // Change scene to login
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLoginPagePath())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }


}
