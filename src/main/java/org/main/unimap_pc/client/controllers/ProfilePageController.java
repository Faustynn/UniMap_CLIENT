package org.main.unimap_pc.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.PasswordChangeRequest;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

@Getter
public class ProfilePageController implements LanguageSupport {
    @FXML private MFXButton btn_homepage;
    @FXML private MFXButton btn_profilepage;
    @FXML private MFXButton btn_subjectpage;
    @FXML private MFXButton btn_teacherspage;
    @FXML private MFXButton btn_settingspage;
    @FXML private MFXButton logoutbtn;

    @FXML private ImageView navi_avatar;
    @FXML private Label navi_username_text;
    @FXML private Label navi_login_text;
    @FXML private Label navi_username_text1;
    @FXML private Label navi_login_text1;
    @FXML private FontAwesomeIcon edit_username;
    @FXML private ImageView avatar_image_view;

    @FXML private Label profile_text;
    @FXML private Label password_text;
    @FXML private Label email_text;
    @FXML private Label change_private_text;

    @FXML private Button btnChangePicture;
    @FXML private Button btnConfirmChangeEmal;
    @FXML private Button btnConfirmChangePass;

    @FXML private TextField changeEmailField;
    @FXML private PasswordField changePasswordField;
    @FXML private PasswordField changeConfirmPasswordField;

    @FXML private ComboBox<String> languageComboBox;

    @FXML private AnchorPane dragArea;

    private final WindowDragHandler windowDragHandler = new WindowDragHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Avatar handling
    private final String customAvatarsFolder = "src/main/resources/org/main/unimap_pc/images/avatares/custom";
    private final String standardAvatarsFolder = "src/main/resources/org/main/unimap_pc/images/avatares";

    @Setter private String avatarFilename;
    private String defaultLanguage;

    @FXML
    private void initialize() {
        try {
            loadUserData();
            windowDragHandler.setupWindowDragging(dragArea);
            displayUserInfo();
            setupLanguageSelector();
        } catch (Exception e) {
            Logger.error("Error during profile page initializing: " + e.getMessage());
        }
    }

    private void loadUserData() {
        try {
            String userData = PreferenceServise.get("USER_DATA").toString();
            JsonNode jsonNode = objectMapper.readTree(userData);
            avatarFilename = jsonNode.get("avatarName").asText();
        } catch (JsonProcessingException e) {
            Logger.error("Error parsing JSON: " + e.getMessage());
        }
    }

    private void displayUserInfo() {
        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            UserService.getInstance().setCurrentUser(user);
            navi_username_text.setText(user.getUsername());
            navi_username_text1.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_login_text1.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
            avatar_image_view.setImage(AppConfig.getAvatar(user.getAvatarName()));

            alignEditUsernameBtn();
        }
    }

    private void setupLanguageSelector() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        defaultLanguage = PreferenceServise.get("LANGUAGE").toString();
        loadCurrentLanguage();
        LanguageManager.changeLanguage(defaultLanguage);
        LanguageManager.getInstance().registerController(this);
        updateUILanguage(LanguageManager.getCurrentBundle());
    }

    private void alignEditUsernameBtn() {
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
                showErrorDialog("Username cannot be empty.");
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
            Logger.error("User email not available. Cannot call backend.");
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", currentUser.getEmail());
        requestBody.put("username", newUsername);

        sendApiRequest(AppConfig.getApiUrl() + "change_username", requestBody, HttpRequest.Builder::PUT,
                response -> {
                    try {
                        currentUser.setUsername(newUsername);
                        UserService.getInstance().setCurrentUser(currentUser);
                        PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));
                        Platform.runLater(() -> {
                            navi_username_text.setText(newUsername);
                            navi_username_text1.setText(newUsername);
                            alignEditUsernameBtn();
                        });
                    } catch (Exception e) {
                        Logger.error("Failed to update username: " + e.getMessage());
                    }
                });
    }

    @FXML
    private void handleChangeEmail() {
        String newEmail = changeEmailField.getText();
        if (newEmail == null || newEmail.isEmpty()) {
            showErrorDialog("Email cannot be empty.");
            return;
        }

        UserModel currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getLogin() == null || currentUser.getLogin().isEmpty()) {
            Logger.error("User login not available. Cannot call backend.");
            return;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("login", currentUser.getLogin());
        requestBody.put("email", newEmail);

        sendApiRequest(AppConfig.getApiUrl() + "change_email", requestBody, HttpRequest.Builder::PUT,
                response -> {
                    try {
                        currentUser.setEmail(newEmail);
                        UserService.getInstance().setCurrentUser(currentUser);
                        PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));
                        Platform.runLater(() -> {
                            changeEmailField.clear();
                            showErrorDialog("Email updated successfully.");
                        });
                    } catch (Exception e) {
                        Logger.error("Failed to update email: " + e.getMessage());
                    }
                });
    }

    @FXML
    private void handleChangeAvatar() {
        Stage stage = new Stage();
        stage.setTitle("Select Avatar");
        stage.initModality(Modality.APPLICATION_MODAL);

        TilePane standardTilePane = new TilePane();
        standardTilePane.setAlignment(Pos.CENTER);
        standardTilePane.setHgap(10);
        standardTilePane.setVgap(10);
        standardTilePane.setPadding(new Insets(20));

        Label standardAvatarLabel = new Label("Standard Avatars");
        standardAvatarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TilePane customTilePane = new TilePane();
        customTilePane.setAlignment(Pos.CENTER);
        customTilePane.setHgap(10);
        customTilePane.setVgap(10);
        customTilePane.setPadding(new Insets(20));

        Label customAvatarLabel = new Label("Your Custom Avatar");
        customAvatarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        loadAvatars(standardTilePane, customTilePane);

        Button uploadAvatarButton = new Button("Upload");
        uploadAvatarButton.setOnAction(event -> uploadNewAvatar(stage, customTilePane));

        VBox mainContainer = new VBox(20,
                standardAvatarLabel, standardTilePane,
                new Separator(),
                customAvatarLabel, customTilePane,
                uploadAvatarButton);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));

        Scene scene = new Scene(new ScrollPane(mainContainer), 600, 500);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void loadAvatars(TilePane standardTilePane, TilePane customTilePane) {
        standardTilePane.getChildren().clear();
        customTilePane.getChildren().clear();

        try {
            // Load from resources directory
            loadAvatarsFromResources(standardTilePane, customTilePane);
            // Load custom avatars from file system
            loadCustomAvatarsFromFileSystem(customTilePane);
        } catch (Exception e) {
            Logger.error("Error loading avatars: " + e.getMessage());
            Label errorLabel = new Label("Error loading avatars");
            errorLabel.setStyle("-fx-text-fill: red;");
            standardTilePane.getChildren().add(errorLabel);
        }
    }

    private void loadAvatarsFromResources(TilePane standardTilePane, TilePane customTilePane) {
        try {
            File folder = new File(standardAvatarsFolder);

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png") ||
                                name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg"));

                boolean hasStandardAvatars = false;

                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName();
                        String baseFileName = fileName.substring(0, fileName.lastIndexOf('.'));

                        // Check if filename is a digit from 0 to 9
                        boolean isStandardAvatar = baseFileName.matches("[0-9]");

                        if (isStandardAvatar) {
                            hasStandardAvatars = true;
                            String imagePath = new File(standardAvatarsFolder, file.getName()).toURI().toString();
                            Image image = new Image(imagePath);

                            StackPane avatarContainer = createAvatarContainer(image, file);
                            standardTilePane.getChildren().add(avatarContainer);
                        }
                    }
                }

                // Add placeholders if no avatars found
                addPlaceholderIfEmpty(standardTilePane, hasStandardAvatars, "No standard avatars found");
            }
        } catch (Exception e) {
            Logger.error("Error loading resource avatars: " + e.getMessage());
        }
    }
    private void loadCustomAvatarsFromFileSystem(TilePane customTilePane) {
        try {
            File customAvatarsDir = new File(customAvatarsFolder);
            if (customAvatarsDir.exists() && customAvatarsDir.isDirectory()) {
                File[] files = customAvatarsDir.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png") ||
                                name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg"));

                if (files != null && files.length > 0) {
                    for (File file : files) {
                        Image image = new Image(file.toURI().toString());
                        StackPane avatarContainer = createAvatarContainer(image, file);
                        customTilePane.getChildren().add(avatarContainer);
                    }
                    addPlaceholderIfEmpty(customTilePane, true, "No custom avatars found");
                } else {
                    addPlaceholderIfEmpty(customTilePane, false, "No custom avatars found");
                }
            }
        } catch (Exception e) {
            Logger.error("Error loading custom avatars folder: " + e.getMessage());
        }
    }

    private void addPlaceholderIfEmpty(TilePane tilePane, boolean hasContent, String message) {
        if (!hasContent) {
            Label noAvatarsLabel = new Label(message);
            noAvatarsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            tilePane.getChildren().add(noAvatarsLabel);
        }
    }

    private StackPane createAvatarContainer(Image image, File file) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);

        Rectangle border = new Rectangle(100, 100);
        border.setArcWidth(10);
        border.setArcHeight(10);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.TRANSPARENT);
        border.setStrokeWidth(3);

        StackPane avatarContainer = new StackPane();
        avatarContainer.getChildren().addAll(imageView, border);

        // Hover effects
        avatarContainer.setOnMouseEntered(e -> {
            border.setStroke(Color.CORNFLOWERBLUE);
            imageView.setEffect(new DropShadow(10, Color.LIGHTBLUE));
        });

        avatarContainer.setOnMouseExited(e -> {
            border.setStroke(Color.TRANSPARENT);
            imageView.setEffect(null);
        });

        // Selection action
        avatarContainer.setOnMouseClicked(event -> {
            try {
                sendAvatarToServer(file, file.getName());
                updateUserAvatar(file.getName(), file);
                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            } catch (Exception e) {
                Logger.error("Error selecting avatar: " + e.getMessage());
                showErrorDialog("Failed to set avatar: " + e.getMessage());
            }
        });

        return avatarContainer;
    }

    private void uploadNewAvatar(Stage parentStage, TilePane customTilePane) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Avatar Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                File destinationDir = new File(customAvatarsFolder);
                if (!destinationDir.exists()) {
                    destinationDir.mkdirs();
                }

                File destinationFile = new File(destinationDir, fileName);
                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                sendAvatarToServer(destinationFile, destinationFile.getName());
                updateUserAvatar(destinationFile.getName(), destinationFile);

                // Refresh the avatar display
                Stage currentStage = (Stage) customTilePane.getScene().getWindow();
                currentStage.close();
                handleChangeAvatar();
            } catch (Exception e) {
                Logger.error("Error uploading avatar: " + e.getMessage());
                showErrorDialog("Failed to upload avatar: " + e.getMessage());
            }
        }
    }

    private void sendAvatarToServer(File avatarFile, String fileName) throws IOException, InterruptedException {
        String backendUrl = AppConfig.getApiUrl() + "change_avatar?fileName=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);

        byte[] fileContent = Files.readAllBytes(avatarFile.toPath());
        String contentType = determineContentType(avatarFile, fileName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileContent))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Logger.error("Server error when changing avatar: " + response.statusCode() + " - " + response.body());
            throw new IOException("Server returned error code: " + response.statusCode() + " - " + response.body());
        }

        // Update user info
        updateUserModelAvatarInfo(fileName);
    }

    private String determineContentType(File file, String fileName) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null) return contentType;

            // Fallback based on extension
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            return switch (extension) {
                case "png" -> "image/png";
                case "jpg", "jpeg" -> "image/jpeg";
                default -> "application/octet-stream";
            };
        } catch (IOException e) {
            Logger.error("Error determining content type: " + e.getMessage());
            return "application/octet-stream";
        }
    }

    private void updateUserModelAvatarInfo(String fileName) {
        UserModel currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setAvatarName(fileName);
            try {
                PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));
            } catch (Exception e) {
                Logger.error("Error updating user data: " + e.getMessage());
            }
        }
    }

    private void updateUserAvatar(String avatarFileName, File avatarFile) {
        try {
            // Check input data
            if (avatarFile == null || !avatarFile.exists()) {
                Logger.error("Avatar file does not exist: " + avatarFile);
                return;
            }

            UserModel currentUser = UserService.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Update user data
                currentUser.setAvatarName(avatarFileName);
                currentUser.setAvatarBinary(Files.readAllBytes(avatarFile.toPath()));
                UserService.getInstance().setCurrentUser(currentUser);

                // Save data to Prefs
                PreferenceServise.put("USER_DATA", objectMapper.writeValueAsString(currentUser));

                // Update UI
                Platform.runLater(() -> {
                    navi_avatar.setImage(new Image(avatarFile.toURI().toString()));
                    avatar_image_view.setImage(new Image(avatarFile.toURI().toString()));
                });

                Logger.info("User avatar updated successfully: " + avatarFileName);
            } else {
                Logger.error("UserModel is null in updateUserAvatar");
            }
        } catch (Exception e) {
            Logger.error("Error updating user avatar: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePass() {
        try {
            String newPassword = changePasswordField.getText();
            String confirmPassword = changeConfirmPasswordField.getText();

            if (newPassword == null || newPassword.isEmpty()) {
                showErrorDialog("Please enter a new password.");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showErrorDialog("Passwords do not match.");
                return;
            }

            String email = UserService.getInstance().getCurrentUser().getEmail();

            PasswordChangeRequest request = new PasswordChangeRequest();
            request.setEmail(email);
            request.setNewPassword(newPassword);

            String requestBody = objectMapper.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getCHANGE_PASSWORD()))
                    .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showErrorDialog("Password changed successfully.");
                changePasswordField.clear();
                changeConfirmPasswordField.clear();
            } else if (response.statusCode() == 404) {
                showErrorDialog("User not found.");
            } else {
                showErrorDialog("Failed to change password: " + response.statusCode());
            }
        } catch (Exception e) {
            Logger.error("An error occurred while changing the password: " + e.getMessage());
            showErrorDialog("An error occurred while changing the password: " + e.getMessage());
        }
    }

    private void loadCurrentLanguage() {
        languageComboBox.setValue(defaultLanguage);

        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                PreferenceServise.put("LANGUAGE", languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                Logger.error("Error changing language: " + e.getMessage());
                showErrorDialog("Failed to change language: " + e.getMessage());
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

    private interface HttpMethodSetter {
        HttpRequest.Builder apply(HttpRequest.Builder builder, HttpRequest.BodyPublisher body);
    }

    private void sendApiRequest(String url, Map<String, String> requestBody,
                                HttpMethodSetter methodSetter,
                                java.util.function.Consumer<String> successHandler) {
        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            HttpRequest request = methodSetter.apply(requestBuilder,
                            HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            String responseBody = response.body();
                            Logger.info("API request successful: " + responseBody);
                            successHandler.accept(responseBody);
                        } else {
                            Logger.error("API request failed: " + response.statusCode());
                        }
                    })
                    .exceptionally(throwable -> {
                        Logger.error("Error calling backend: " + throwable.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            Logger.error("Error creating JSON request: " + e.getMessage());
        }
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

    private void navigateToPage(String resourcePath) {
        try {
            Stage currentStage = (Stage) btn_homepage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resourcePath)));
            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        // Clear the user data
        PreferenceServise.deletePreferences();
        PreferenceServise.put("REMEMBER", false);
        CacheService.clearCache();

        // Change scene to login
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLOGIN_PAGE_PATH())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }
}