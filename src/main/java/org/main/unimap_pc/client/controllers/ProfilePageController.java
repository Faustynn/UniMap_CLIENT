package org.main.unimap_pc.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.PasswordChangeRequest;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class ProfilePageController implements LanguageSupport {

    public ImageView navi_avatar;
    public Label navi_username_text;
    public Label navi_login_text;
    public Label navi_username_text1;
    public Label navi_login_text1;
    public Button btnChangePicture;
    public TextField changeEmailField;
    public PasswordField changePasswordField;
    public Button btnConfirmChangePass;
    public Label profile_text;
    public Label password_text;
    public Label email_text;
    public Label change_private_text;
    public FontAwesomeIcon edit_username;
    public Button btnConfirmChangeEmal;
    public PasswordField changeConfirmPasswordField;

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
    private ImageView avatar_image_view;

    @FXML
    private void initialize() {
        try {
            UserModel user = UserService.getInstance().getCurrentUser();
            if (user != null) {
                UserService.getInstance().setCurrentUser(user);
                navi_username_text.setText(user.getUsername());
                navi_username_text1.setText(user.getUsername());
                navi_login_text.setText(user.getLogin());
                navi_login_text1.setText(user.getLogin());
                navi_avatar.setImage(AppConfig.getAvatar(user.getAvatar()));
                avatar_image_view.setImage(AppConfig.getAvatar(user.getAvatar()));

                // set x coordinate for edit_profile icon in the end of navi_username_text1
                alignEditUsernameBtn();
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
            e.printStackTrace();
        }
    }

    private void alignEditUsernameBtn(){
        Platform.runLater(() -> {
            double textWidth = navi_username_text1.getLayoutBounds().getWidth();
            double textX = navi_username_text1.getLayoutX();
            double iconWidth = edit_username.getLayoutBounds().getWidth();
            edit_username.setLayoutX(textX + textWidth + 5); // 5 is a small padding
            edit_username.setLayoutY(navi_username_text1.getLayoutY() + (navi_username_text1.getLayoutBounds().getHeight() - iconWidth)/2);

        });
    }

    @FXML
    private void handleChangeUsername() {
        Stage stage = new Stage();
        stage.setTitle("Change Username");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter new username");

        Button confirmButton = new Button("Confirm");

        confirmButton.setOnAction(event -> {
            String newUsername = usernameField.getText().trim();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Username cannot be empty.");
                alert.showAndWait();
            }
        });

        vbox.getChildren().addAll(usernameField, confirmButton);

        Scene scene = new Scene(vbox, 300, 150);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void updateUsername(String newUsername) {
        UserModel currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            System.err.println("User email not available. Cannot call backend.");
            return;
        }

        String backendUrl = AppConfig.getApiUrl() + "change_username";
        HttpClient httpClient = HttpClient.newBuilder().build();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", currentUser.getEmail());
        requestBody.put("username", newUsername);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson;
        try {
            requestBodyJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            System.err.println("Error creating JSON request body: " + e.getMessage());
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String responseBody = response.body();
                            System.out.println("Username updated on backend: " + responseBody);
                            currentUser.setUsername(newUsername);
                            UserService.getInstance().setCurrentUser(currentUser);
                            PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));
                            Platform.runLater(() -> {
                                navi_username_text.setText(newUsername);
                                navi_username_text1.setText(newUsername); // Line 208
                                alignEditUsernameBtn();
                            });
                        } catch (Exception e) {
                            System.err.println("Failed to parse backend response: " + e.getMessage());
                        }

                    } else {
                        System.err.println("Backend returned error: " + response.statusCode());
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Error calling backend: " + throwable.getMessage());
                    return null;
                });
    }

    @FXML
    private void handleChangeEmail() {
        String newEmail = changeEmailField.getText();
        UserModel currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            System.err.println("User email not available. Cannot call backend.");
            return;
        }

        String backendUrl = AppConfig.getApiUrl() + "change_email";
        HttpClient httpClient = HttpClient.newBuilder().build();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("login", currentUser.getLogin());
        requestBody.put("email", newEmail);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson;
        try {
            requestBodyJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            System.err.println("Error creating JSON request body: " + e.getMessage());
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String responseBody = response.body();
                            System.out.println("Email updated on backend: " + responseBody);
                            currentUser.setEmail(newEmail);
                            UserService.getInstance().setCurrentUser(currentUser);
                            PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));
                            Platform.runLater(() -> {
//                                navi_username_text.setText(newUsername);
//                                navi_username_text1.setText(newUsername); // Line 208
//                                alignEditUsernameBtn();
                            });
                        } catch (Exception e) {
                            System.err.println("Failed to parse backend response: " + e.getMessage());
                        }

                    } else {
                        System.err.println("Backend returned error: " + response.statusCode());
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Error calling backend: " + throwable.getMessage());
                    return null;
                });
    }

    private int avatarID; // Assuming you have avatarID defined somewhere in your controller
    private ImageView avatarImageView; //ImageView where you display the avatar in the main window.

    @FXML
    private Image profile_picture;

    @FXML
    private void handleChangeAvatar() {
        Stage stage = new Stage();
        stage.setTitle("Select Avatar");
        stage.initModality(Modality.APPLICATION_MODAL);

        TilePane tilePane = new TilePane();
        tilePane.setAlignment(javafx.geometry.Pos.CENTER);
        tilePane.setHgap(10);
        tilePane.setVgap(10);

        File folder = new File(getClass().getResource("/org/main/unimap_pc/images/avatares/").getFile());
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                List<ImageView> avatarImageViews = new ArrayList<>();
                for (File file : files) {
                    String imagePath = "/org/main/unimap_pc/images/avatares/" + file.getName();
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);

                    int avatarId = Integer.parseInt(file.getName().replace(".png", ""));

                    // so here images 2 and 3 will be blocked for casual user and there will be access to them only for premium users and admin
                    if (avatarId == 2 || avatarId == 3) {
                        if (UserService.getInstance().getCurrentUser().isAdmin() || UserService.getInstance().getCurrentUser().isPremium()) {
                            // Admin or premium user, allow selection
                            imageView.setOnMouseClicked(event -> {
                                updateAvatar(avatarId);
                                stage.close();
                            });
                        } else {
                            // Casual user, block selection and provide feedback
                            imageView.setOpacity(0.5); // Visually indicate it's disabled
                            imageView.setOnMouseClicked(event -> {
                                // Optionally show an alert or message
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Premium Avatar");
                                alert.setHeaderText(null);
                                alert.setContentText("This avatar is only available for premium users or administrators.");
                                alert.showAndWait();
                            });
                        }
                    } else {
                        // Regular avatar, allow selection for all users
                        imageView.setOnMouseClicked(event -> {
                            updateAvatar(avatarId);
                            stage.close();
                        });
                    }
                    avatarImageViews.add(imageView);
                }
                tilePane.getChildren().addAll(avatarImageViews);
            }
        } else {
            System.err.println("Avatar directory not found.");
        }

        Scene scene = new Scene(tilePane, 600, 400);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void updateAvatar(int selectedAvatarID) {
        this.avatarID = selectedAvatarID;
        String imagePath = "/org/main/unimap_pc/images/avatares/" + avatarID + ".png";
        Image newAvatar = new Image(getClass().getResourceAsStream(imagePath));
        if(avatarImageView != null){
            avatarImageView.setImage(newAvatar);
        }

        UserModel currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isEmpty()) {
            System.err.println("User email not available. Cannot call backend.");
            return;
        }

        String backendUrl = AppConfig.getApiUrl() + "change_avatar";
        HttpClient httpClient = HttpClient.newBuilder().build();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", currentUser.getEmail());
        requestBody.put("avatarPath", Integer.toString(avatarID)); // Send only the ID

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson;
        try {
            requestBodyJson = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            System.err.println("Error creating JSON request body: " + e.getMessage());
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String responseBody = response.body();
                            System.out.println("Avatar updated on backend: " + responseBody);
                            currentUser.setAvatar(Integer.toString(avatarID)); // Store only the ID
                            UserService.getInstance().setCurrentUser(currentUser);
                            PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));
                            avatar_image_view.setImage(AppConfig.getAvatar(currentUser.getAvatar()));
                        } catch (Exception e) {
                            System.err.println("Failed to parse backend response: " + e.getMessage());
                        }

                    } else {
                        System.err.println("Backend returned error: " + response.statusCode());
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Error calling backend: " + throwable.getMessage());
                    return null;
                });
        }

    @FXML
    private void handleChangePass() {
        try {
            String newPassword = changePasswordField.getText();
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
                    .uri(URI.create(AppConfig.getChangePassword()))
                    .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println(httpRequest);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                showErrorDialog("Password changed successfully.");
                changePasswordField.clear();
            } else if (response.statusCode() == 404) {
                showErrorDialog("User not found.");
            } else if (response.statusCode() == 302) {
                String location = response.headers().firstValue("Location").orElse("Location header not found");
                System.out.println("Location Header: " + location);
                showErrorDialog("Redirect received. Location: " + location + ". Check server logs.");
            } else {
                showErrorDialog("Failed to change password: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("An error occurred while changing the password: " + e.getMessage());
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
                e.printStackTrace();
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


}
