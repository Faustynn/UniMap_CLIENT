package org.main.unimap_pc.client.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.NewsModel;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.DataFetcher;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.*;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

@NoArgsConstructor
public class HomePageController implements LanguageSupport {
    @FXML private AnchorPane dragArea;
    @FXML private MFXButton logoutbtn;
    @FXML private MFXButton btn_homepage;
    @FXML private MFXButton btn_profilepage;
    @FXML private MFXButton btn_subjectpage;
    @FXML private MFXButton btn_teacherspage;
    @FXML private MFXButton btn_settingspage;

    @FXML private ScrollPane scrollPane_news;
    @FXML private AnchorPane pane_for_news;
    @FXML private FontAwesomeIcon refresh_news;
    @FXML private Label news_upd_text;

    @FXML private Label utils_text;
    @FXML private Label descriptFIITDISCORD;
    @FXML private Label descriptFXcom;
    @FXML private Label descriptMladost;
    @FXML private Label descriptFIITTelegram;

    @FXML private ComboBox<String> languageComboBox;

    @FXML private Label navi_login_text;
    @FXML private Label navi_username_text;
    @FXML private ImageView navi_avatar;

    private final WindowDragHandler windowDragHandler = new WindowDragHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;
    private String refreshToken;
    private String userData;
    private String cachedLanguage;

    private boolean isLanguageSupportInitialized = false;
    private String defaultLanguage;

    @FXML
    private void initialize() {
        try {
            windowDragHandler.setupWindowDragging(dragArea);
            setupSceneResizeListeners();

            SseManager sseManager = new SseManager();
            sseManager.registerHomePageController(this);
            sseManager.connectToSSEServer();

            sseManager.addNewsListener(this::updateNews);

            displayLoadingIndicator();
            loadNews();

            if (!isLanguageSupportInitialized) {
                setupLanguageSelector();
                isLanguageSupportInitialized = true;
            }
            loadUserData();
        } catch (Exception e) {
            Logger.error("Error during initialization: " + e.getMessage());
            System.err.println("Error during initialization: " + e.getMessage());
        }
    }

    private void loadUserData() {
        accessToken = PreferenceServise.get("ACCESS_TOKEN").toString();
        refreshToken = PreferenceServise.get("REFRESH_TOKEN").toString();
        userData = PreferenceServise.get("USER_DATA").toString();
        cachedLanguage = PreferenceServise.get("LANGUAGE").toString();

        UserModel user = initUser(userData);
        try {
            if (user != null) {
                UserService.getInstance().setCurrentUser(user);
                navi_username_text.setText(user.getUsername());
                navi_login_text.setText(user.getLogin());
                navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
            } else {
     //           System.out.println("User is null");
            }
        } catch (Exception e) {
            Logger.error("Error while setting user data: " + e.getMessage());
            System.err.println("Error while setting user data: " + e.getMessage());
            showErrorDialog("An error occurred while loading user data. Please try again.");
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

    private void loadCurrentLanguage() {
        if(defaultLanguage !=null){
            languageComboBox.setValue(defaultLanguage);
        }else {
            String selectedLanguage = PreferenceServise.get(AppConfig.getLANGUAGE_KEY()).toString();
            languageComboBox.setValue(selectedLanguage);
        }

        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                showErrorDialog("Error changing language: " + e.getMessage());
                Logger.error("Error changing language: " + e.getMessage());
                loadCurrentLanguage();
            }
        });
    }

    private void setupSceneResizeListeners() {
        Scene scene = dragArea.getScene();
        if (scene != null) {
            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                // TODO: Implement resize logic
            });
            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                // TODO: Implement resize logic
            });
        }
    }

    private void loadNews() {
        SseManager sseManager = new SseManager();
        sseManager.registerHomePageController(this);
        sseManager.connectToSSEServer();

        sseManager.addNewsListener(newsList -> Platform.runLater(() -> {
            if (newsList != null && !newsList.isEmpty()) {
                displayNews(newsList);
            } else {
                displayNoNewsMessage(new VBox());
            }
        }));
    }

//    private void loadNews() {
//        CompletableFuture<String> newsJsonFuture = DataFetcher.fetchNews();
//
//        newsJsonFuture.thenAccept(newsJson -> {
//        //    System.out.println("Fetched news: " + newsJson);
//
//            if (newsJson != null && !newsJson.isEmpty()) {
//                try {
//                    List<NewsModel> newsList = objectMapper.readValue(newsJson, new TypeReference<List<NewsModel>>() {});
//                    Platform.runLater(() -> {
//                        displayNews(newsList);
//                    });
//                } catch (Exception e) {
//                    Logger.error("Failed to parse news JSON: " + e.getMessage());
//                    Platform.runLater(this::displayNewsLoadError);
//                }
//            } else {
//                Logger.error("News response is empty or null.");
//                Platform.runLater(this::displayNewsLoadError);
//            }
//        }).exceptionally(ex -> {
//            Logger.error("Error fetching news: " + ex.getMessage());
//            Platform.runLater(this::displayNewsLoadError);
//            return null;
//        });
//    }

    private void displayLoadingIndicator() {
        Label loadingLabel = new Label("Loading news...");
        loadingLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
        pane_for_news.getChildren().add(loadingLabel);
    }

    private void displayNewsLoadError() {
        Label errorLabel = new Label("Failed to load news, please check your internet connection!");
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        errorLabel.setPadding(new Insets(20, 10, 10, 10));
        pane_for_news.getChildren().add(errorLabel);
    }

    private void displayNews(List<NewsModel> newsList) {
        pane_for_news.getChildren().clear();

        VBox newsContainer = new VBox(5);
        newsContainer.setPrefWidth(pane_for_news.getPrefWidth());
        VBox.setVgrow(newsContainer, Priority.ALWAYS);

        if (newsList.isEmpty()) {
            displayNoNewsMessage(newsContainer);
        } else {
            for (NewsModel news : newsList) {
                AnchorPane newsItem = createNewsItem(news);
                newsContainer.getChildren().add(newsItem);
            }
        }

        pane_for_news.setStyle("-fx-background-color: #191C22;");
        pane_for_news.getChildren().add(newsContainer);
        pane_for_news.setPrefHeight(newsList.size() * (140 + 8));
        pane_for_news.setMinHeight(516);
    }

    private void displayNoNewsMessage(VBox container) {
        Label noNewsLabel = new Label("No news available");
        noNewsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        VBox.setMargin(noNewsLabel, new Insets(150, 0, 0, 150));
        container.getChildren().add(noNewsLabel);
    }

    private AnchorPane createNewsItem(NewsModel news) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(140);
        card.setPrefWidth(pane_for_news.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: #2f3541;");

        // News title
        String title = formatTitle(news.getTitle());
        Label titleLabel = createTitleLabel(title);
        card.getChildren().add(titleLabel);

        // News date
        Label dateLabel = createDateLabel(news.getDate_of_creation());
        card.getChildren().add(dateLabel);

        // News content
        Label contentLabel = createContentLabel(news.getContent());
        card.getChildren().add(contentLabel);

        return card;
    }

    private String formatTitle(String title) {
        return title.length() > 50 ? title.substring(0, 50) + "..." : title;
    }

    private Label createTitleLabel(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        AnchorPane.setTopAnchor(titleLabel, 15.0);
        AnchorPane.setLeftAnchor(titleLabel, 20.0);
        return titleLabel;
    }

    private Label createDateLabel(String dateString) {
        DateTimeFormatter originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter newFormat = DateTimeFormatter.ofPattern("d/MM/yy");
        LocalDateTime dateTime = LocalDateTime.parse(dateString, originalFormat);
        String formattedDate = dateTime.format(newFormat);

        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #90A4AE;");
        AnchorPane.setTopAnchor(dateLabel, 15.0);
        AnchorPane.setRightAnchor(dateLabel, 20.0);
        return dateLabel;
    }

    private Label createContentLabel(String content) {
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0BEC5;");
        contentLabel.setPrefWidth(pane_for_news.getPrefWidth() - 40);
        contentLabel.setPrefHeight(80);
        contentLabel.setWrapText(true);
        contentLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        AnchorPane.setTopAnchor(contentLabel, 50.0);
        AnchorPane.setLeftAnchor(contentLabel, 20.0);
        return contentLabel;
    }

    private UserModel initUser(String userData) {
        if (userData == null) {
            return null;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(userData);

            String id = jsonNode.get("id").asText();
            String login = jsonNode.get("login").asText();
            String email = jsonNode.get("email").asText();
            String username = jsonNode.get("username").asText();
            boolean admin = jsonNode.get("admin").asBoolean();
            boolean premium = jsonNode.get("premium") != null && jsonNode.get("premium").asBoolean();
            String avatarBinary = jsonNode.get("avatarBinary").asText();
            String avatarFileName = jsonNode.get("avatarName").asText();

            if (avatarBinary != null && !avatarBinary.isEmpty() && !avatarBinary.equals("null")) {
                avatarFileName = processAndSaveAvatar(avatarBinary, avatarFileName);
            } else if (avatarFileName == null || avatarFileName.isEmpty() || avatarFileName.equals("null")) {
                avatarFileName = "2.png"; // Default if no name
            }

            return new UserModel(id, username, email, login, admin, premium, avatarBinary.getBytes(), avatarFileName);
        } catch (Exception e) {
            Logger.error("Error parsing user data: " + e.getMessage());
            return null;
        }
    }

    private String processAndSaveAvatar(String avatarBinary, String avatarFileName) {
        try {
            byte[] avatarBytes = java.util.Base64.getDecoder().decode(avatarBinary);
            String avatarFolderPath = determineAvatarFolderPath(avatarFileName);
            File avatarFolder = new File(avatarFolderPath);

            // Create directory if it doesn't exist
            if (!avatarFolder.exists()) {
                avatarFolder.mkdirs();
            }

            File avatarFile = new File(avatarFolder, avatarFileName);
            try (FileOutputStream fos = new FileOutputStream(avatarFile)) {
                fos.write(avatarBytes);
        //        System.out.println("Avatar saved to: " + avatarFile.getAbsolutePath());
                return avatarFileName;
            } catch (IOException e) {
                Logger.error("Error saving avatar file: " + e.getMessage());
                return "2.png"; // Default on error
            }
        } catch (Exception e) {
            Logger.error("Error processing avatar binary: " + e.getMessage());
            return "2.png"; // Default on error
        }
    }

    private String determineAvatarFolderPath(String avatarFileName) {
        if (isDefaultAvatar(avatarFileName)) {
            return "src/main/resources/org/main/unimap_pc/images/avatares";
        } else {
            return "src/main/resources/org/main/unimap_pc/images/avatares/custom";
        }
    }

    private boolean isDefaultAvatar(String avatarFileName) {
        return avatarFileName.matches("[0-9]\\.png");
    }

    @FXML
    private void handleLogout() throws IOException {
        // Clear the user data
        PreferenceServise.deletePreferences();
        PreferenceServise.put("REMEMBER", false);
        CacheService.clearCache();

        // Change scene to login
        navigateToPage(AppConfig.getLOGIN_PAGE_PATH());
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

    @FXML
    private void handleRefreshNewsClick() {
        pane_for_news.getChildren().clear();
        displayLoadingIndicator();
        loadNews();
    }

    private void navigateToPage(String resourcePath) {
        try {
            Stage currentStage = (Stage) dragArea.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resourcePath)));
            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load page: " + e.getMessage());
            System.err.println("Failed to load page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private void fiit_discord_handler() {
        openWebLink("https://discord.gg/dX48acpNS8", "Discord");
    }

    @FXML
    private void fx_com_handler() {
        openWebLink("https://www.notion.so/FX-com-54cdb158085e4377b832ece310a5603d", "FXcom");
    }

    @FXML
    private void mladost_handler() {
        openWebLink("https://protective-april-ef1.notion.site/SD-Mladost-abe968a31d404360810b53acbbb357cc", "Mladost");
    }

    @FXML
    private void fiit_telegram_handler() {
        openWebLink("https://t.me/fiitstu", "FIIT Telegram");
    }

    private void openWebLink(String url, String linkName) {
        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (IOException e) {
            Logger.error("Failed to open " + linkName + " link: " + e.getMessage());
            showErrorDialog("Failed to open " + linkName + " link: " + e.getMessage());
        }
    }

    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        logoutbtn.setText(languageBundle.getString("logout"));
        btn_homepage.setText(languageBundle.getString("homepage"));
        btn_profilepage.setText(languageBundle.getString("profilepage"));
        btn_subjectpage.setText(languageBundle.getString("subjectpage"));
        btn_teacherspage.setText(languageBundle.getString("teacherspage"));
        btn_settingspage.setText(languageBundle.getString("settingspage"));
        languageComboBox.setPromptText(languageBundle.getString("language.combobox"));
        news_upd_text.setText(languageBundle.getString("news.updates"));
        utils_text.setText(languageBundle.getString("utils"));
        descriptFIITDISCORD.setText(languageBundle.getString("descriptFIITDISCORD"));
        descriptFXcom.setText(languageBundle.getString("descriptFXcom"));
        descriptMladost.setText(languageBundle.getString("descriptMladost"));
        descriptFIITTelegram.setText(languageBundle.getString("descriptFIITTelegram"));
    }

    public void updateNews(List<NewsModel> newsList) {
        Platform.runLater(() -> {
            displayNews(newsList);
        });
    }
}