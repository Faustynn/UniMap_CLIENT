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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    private final String customAvatarsFolder = "src/main/resources/org/main/unimap_pc/images/avatares/custom";
    private final String standartAvatarsFolder = "src/main/resources/org/main/unimap_pc/images/avatares";
    private String AVATAR_FILENAME;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void initialize() {
        try {
            try {
                String userData = PreferenceServise.get("USER_DATA").toString();
                JsonNode jsonNode = objectMapper.readTree(userData);
                AVATAR_FILENAME = jsonNode.get("avatarName").asText();
            } catch (JsonProcessingException e) {
                Logger.error("Error parsing JSON: " + e.getMessage());
            }

            UserModel user = UserService.getInstance().getCurrentUser();
            if (user != null) {
                UserService.getInstance().setCurrentUser(user);
                navi_username_text.setText(user.getUsername());
                navi_username_text1.setText(user.getUsername());
                navi_login_text.setText(user.getLogin());
                navi_login_text1.setText(user.getLogin());
                navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
                System.out.println("QWERTY Avatar name: " + user.getAvatarName());
                avatar_image_view.setImage(AppConfig.getAvatar(user.getAvatarName()));

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
            Logger.error("Error during profile page initializing: " + e.getMessage());
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
            Logger.error("Error creating JSON request body: " + e.getMessage());
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson))
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
                                navi_username_text1.setText(newUsername);
                                alignEditUsernameBtn();
                            });
                        } catch (Exception e) {
                            Logger.error("Failed to parse backend response: " + e.getMessage());
                        }

                    } else {
                        Logger.error("Backend returned error: " + response.statusCode());
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Error calling backend: " + throwable.getMessage());
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
            Logger.error("Error creating JSON request body: " + e.getMessage());
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson))
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
                                // UI updates if needed
                            });
                        } catch (Exception e) {
                            Logger.error("Failed to parse backend response: " + e.getMessage());
                        }

                    } else {
                        Logger.error("Backend returned error: " + response.statusCode());
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Error calling backend: " + throwable.getMessage());
                    return null;
                });
    }

    @FXML
    private void handleChangeAvatar() {
        Stage stage = new Stage();
        stage.setTitle("Select Avatar");
        stage.initModality(Modality.APPLICATION_MODAL);

        TilePane tilePane = new TilePane();
        tilePane.setAlignment(Pos.CENTER);
        tilePane.setHgap(10);
        tilePane.setVgap(10);
        tilePane.setPadding(new Insets(20));

        // Load standard avatars
        loadStandardAvatars(tilePane);

        Label customAvatarLabel = new Label("Your Custom Avatar");
        customAvatarLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        StackPane customAvatarContainer = new StackPane();
        customAvatarContainer.setAlignment(Pos.CENTER);
        customAvatarContainer.setPadding(new Insets(10));

        File customAvatarFile = new File("src/main/resources/org/main/unimap_pc/images/avatares/custom/" + AVATAR_FILENAME);
        if (customAvatarFile.exists()) {
            loadCustomAvatars(customAvatarContainer);
        } else {
            Label nothingAvatarLabel = new Label("No custom avatar found");
            nothingAvatarLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            customAvatarContainer.getChildren().add(nothingAvatarLabel);
        }

        Button uploadAvatarButton = new Button("Upload New Avatar");
        uploadAvatarButton.setOnAction(event -> uploadNewAvatar(stage, customAvatarContainer));

        VBox mainContainer = new VBox(20, tilePane, new Separator(), customAvatarLabel, customAvatarContainer, uploadAvatarButton);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));

        Scene scene = new Scene(new ScrollPane(mainContainer), 600, 500);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void loadStandardAvatars(TilePane tilePane) {
        tilePane.getChildren().clear();

        try {
            String directoryPath = "/org/main/unimap_pc/images/avatares";
            File folder = new File(getClass().getResource(directoryPath).toURI());

            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png") ||
                                name.toLowerCase().endsWith(".jpg") ||
                                name.toLowerCase().endsWith(".jpeg"));

                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String imagePath = directoryPath + "/" + file.getName();
                        Image image = new Image(getClass().getResourceAsStream(imagePath));

                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(100);
                        imageView.setFitHeight(100);

                        // avatar frame with hover effect
                        StackPane avatarContainer = new StackPane();
                        Rectangle border = new Rectangle(100, 100);
                        border.setArcWidth(10);
                        border.setArcHeight(10);
                        border.setFill(Color.TRANSPARENT);
                        border.setStroke(Color.TRANSPARENT);
                        border.setStrokeWidth(3);

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

                        // Avatar selection
                        avatarContainer.setOnMouseClicked(event -> {
                            try {
                                sendAvatarToServer(file, file.getName());

                                // Update UI
                                updateUserAvatar(file.getName(), file);

                                // Close selection window
                                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
                            } catch (Exception e) {
                                Logger.error("Error selecting avatar: " + e.getMessage());
                                showErrorDialog("Failed to set avatar: " + e.getMessage());
                            }
                        });

                        tilePane.getChildren().add(avatarContainer);
                    }
                } else {
                    Label noAvatarsLabel = new Label("No standard avatars found");
                    noAvatarsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    tilePane.getChildren().add(noAvatarsLabel);
                }
            }
        } catch (Exception e) {
            Logger.error("Error loading standard avatars: " + e.getMessage());
            Label errorLabel = new Label("Error loading avatars");
            errorLabel.setStyle("-fx-text-fill: red;");
            tilePane.getChildren().add(errorLabel);
        }
    }

    private void loadCustomAvatars(StackPane tilePane) {
        tilePane.getChildren().clear();

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

                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(100);
                        imageView.setFitHeight(100);

                        // avatar frame with hover effect
                        StackPane avatarContainer = new StackPane();
                        Rectangle border = new Rectangle(100, 100);
                        border.setArcWidth(10);
                        border.setArcHeight(10);
                        border.setFill(Color.TRANSPARENT);
                        border.setStroke(Color.TRANSPARENT);
                        border.setStrokeWidth(3);

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

                        // Avatar selection
                        avatarContainer.setOnMouseClicked(event -> {
                            try {
                                sendAvatarToServer(file, file.getName());
                                updateUserAvatar(file.getName(), file);

                                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
                            } catch (Exception e) {
                                Logger.error("Error selecting custom avatar: " + e.getMessage());
                                showErrorDialog("Failed to set avatar: " + e.getMessage());
                            }
                        });

                        tilePane.getChildren().add(avatarContainer);
                    }
                } else {
                    Label noAvatarsLabel = new Label("No custom avatars found");
                    noAvatarsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    tilePane.getChildren().add(noAvatarsLabel);
                }
            } else {
                Label noAvatarsLabel = new Label("Custom avatars directory not found");
                noAvatarsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                tilePane.getChildren().add(noAvatarsLabel);
            }
        } catch (Exception e) {
            Logger.error("Error loading custom avatars: " + e.getMessage());
            Label errorLabel = new Label("Error loading custom avatars");
            errorLabel.setStyle("-fx-text-fill: red;");
            tilePane.getChildren().add(errorLabel);
        }
    }

    private void uploadNewAvatar(Stage parentStage, StackPane customAvatarContainer) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Avatar Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(parentStage);
        if (selectedFile != null) {
            try {
                File destinationFile;
                if (selectedFile.getName().equalsIgnoreCase(AVATAR_FILENAME)) {
                    File customAvatarsDir = new File(customAvatarsFolder);
                    if (!customAvatarsDir.exists()) {
                        customAvatarsDir.mkdirs();
                    }
                    destinationFile = new File(customAvatarsDir, AVATAR_FILENAME);
                } else {
                    File standardAvatarsDir = new File(standartAvatarsFolder);
                    if (!standardAvatarsDir.exists()) {
                        standardAvatarsDir.mkdirs();
                    }
                    destinationFile = new File(standardAvatarsDir, selectedFile.getName());
                }

                Files.copy(selectedFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sendAvatarToServer(destinationFile, destinationFile.getName());
                updateUserAvatar(destinationFile.getName(), destinationFile);

                customAvatarContainer.getChildren().clear();
                Image newCustomImage = new Image(destinationFile.toURI().toString());
                ImageView newCustomImageView = new ImageView(newCustomImage);
                newCustomImageView.setFitWidth(120);
                newCustomImageView.setFitHeight(120);

                // Add selection functionality to new custom avatar
                newCustomImageView.setOnMouseClicked(event -> {
                    try {
                        sendAvatarToServer(destinationFile, destinationFile.getName());
                        updateUserAvatar(destinationFile.getName(), destinationFile);
                        ((Stage) customAvatarContainer.getScene().getWindow()).close();
                    } catch (Exception e) {
                        Logger.error("Error selecting custom avatar: " + e.getMessage());
                        showErrorDialog("Failed to set avatar: " + e.getMessage());
                    }
                });

                // Add hover effects
                Rectangle border = new Rectangle(120, 120);
                border.setArcWidth(10);
                border.setArcHeight(10);
                border.setFill(Color.TRANSPARENT);
                border.setStroke(Color.TRANSPARENT);
                border.setStrokeWidth(3);

                StackPane avatarWrapper = new StackPane(newCustomImageView, border);

                avatarWrapper.setOnMouseEntered(e -> {
                    border.setStroke(Color.CORNFLOWERBLUE);
                    newCustomImageView.setEffect(new DropShadow(10, Color.LIGHTBLUE));
                });

                avatarWrapper.setOnMouseExited(e -> {
                    border.setStroke(Color.TRANSPARENT);
                    newCustomImageView.setEffect(null);
                });

                customAvatarContainer.getChildren().add(avatarWrapper);
                loadCustomAvatars(customAvatarContainer);

            } catch (Exception e) {
                Logger.error("Error uploading avatar: " + e.getMessage());
                showErrorDialog("Failed to upload avatar: " + e.getMessage());
            }
        }
    }

    private void sendAvatarToServer(File avatarFile, String fileName) throws IOException, InterruptedException {
        String backendUrl = AppConfig.getApiUrl() + "change_avatar?fileName=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        HttpClient httpClient = HttpClient.newHttpClient();

        byte[] fileContent = Files.readAllBytes(avatarFile.toPath());
        String contentType = Files.probeContentType(avatarFile.toPath());

        System.out.println("File content type: " + contentType);
        System.out.println("File size: " + fileContent.length + " bytes");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(backendUrl))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(fileContent))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response Status Code: " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new IOException("Server returned error code: " + response.statusCode() + " - " + response.body());
        }

        // Process server response
        String responseBody = response.body();
        System.out.println("Avatar changed successfully, server response: " + responseBody);
        Logger.info("Avatar changed successfully, server response: " + responseBody);

        // Update user info
        UserModel currentUser = UserService.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.setAvatarName(fileName);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
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
                ObjectMapper objectMapper = new ObjectMapper();
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

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(request);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(AppConfig.getChangePassword()))
                    .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println(httpRequest);
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                showErrorDialog("Password changed successfully.");
                changePasswordField.clear();
                changeConfirmPasswordField.clear();
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
            Logger.error("An error occurred while changing the password: " + e.getMessage());
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
            }catch (Exception e) {
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