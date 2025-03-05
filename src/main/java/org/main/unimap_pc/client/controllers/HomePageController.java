package org.main.unimap_pc.client.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.NewsModel;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.DataFetcher;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class HomePageController implements LanguageSupport {

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

    private String accessToken;
    private String refreshToken;
    private String userData;
    private String cachedLanguage;

    @FXML
    private void initialize() {
        try {
            loadNews();
            //   System.out.println("Loaded News");
            languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
            loadCurrentLanguage();

            accessToken = PreferenceServise.get("ACCESS_TOKEN").toString();
            refreshToken = PreferenceServise.get("REFRESH_TOKEN").toString();
            userData = PreferenceServise.get("USER_DATA").toString();
            cachedLanguage = PreferenceServise.get("LANGUAGE").toString();

            UserModel user = initUser(userData);
            if (user != null) {
                UserService.getInstance().setCurrentUser(user);
                navi_username_text.setText(user.getUsername());
                navi_login_text.setText(user.getLogin());
                navi_avatar.setImage(AppConfig.getAvatar(user.getAvatar()));
            }

            LanguageManager.changeLanguage(cachedLanguage);
            LanguageManager.getInstance().registerController(this);
            updateUILanguage(LanguageManager.getCurrentBundle());

        } catch (Exception e) {
            e.printStackTrace();
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
    }

    private void loadCurrentLanguage() {
        String selectedLanguage = PreferenceServise.get(AppConfig.getLANGUAGE_KEY()).toString();
        languageComboBox.setValue(selectedLanguage);

        // listener for lang. editing
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = (String) languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                showErrorDialog("Error changing language: " + e.getMessage());
                loadCurrentLanguage();
            }
        });
    }

    @FXML
    private ScrollPane scrollPane_news;
    @FXML
    private AnchorPane pane_for_news;

    private void loadNews() {
        CompletableFuture<String> newsJsonFuture = DataFetcher.fetchNews();

        newsJsonFuture.thenAccept(newsJson -> {
            if (newsJson != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<NewsModel> newsList = objectMapper.readValue(newsJson, new TypeReference<List<NewsModel>>() {});
                    Platform.runLater(() -> {
                        Label loadingLabel = new Label("Loading news...");
                        loadingLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
                        pane_for_news.getChildren().add(loadingLabel);

                        displayNews(newsList);

                    });
                } catch (Exception e) {
                    System.err.println("Failed to parse news JSON: " + e.getMessage());
                }
            } else {
                System.err.println("Failed to load news.");
                Label errorLabel = new Label("Failed to load news, please check your internet connection!");
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                errorLabel.setPadding(new Insets(20, 10, 10, 10));
                pane_for_news.getChildren().add(errorLabel);
            }
        });

    }

    private void displayNews(List<NewsModel> newsList) {
        pane_for_news.getChildren().clear();

        VBox newsContainer = new VBox(5);
        newsContainer.setPrefWidth(pane_for_news.getPrefWidth());
        VBox.setVgrow(newsContainer, Priority.ALWAYS);

        for (NewsModel news : newsList) {
            AnchorPane newsItem = createNewsItem(news);
            newsContainer.getChildren().add(newsItem);
        }

        pane_for_news.setStyle("-fx-background-color: #191C22;");
        pane_for_news.getChildren().add(newsContainer);
        pane_for_news.setPrefHeight(newsList.size()*(140+8));
        pane_for_news.setMinHeight(444);

    }

    private AnchorPane createNewsItem(NewsModel news) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(140);
        card.setPrefWidth(pane_for_news.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: #2f3541;");

        // Заголовок новости
        String title = news.getTitle();
        if (title.length() > 50) {
            title = title.substring(0, 50) + "...";
        }
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
        card.getChildren().add(titleLabel);
        AnchorPane.setTopAnchor(titleLabel, 15.0);
        AnchorPane.setLeftAnchor(titleLabel, 20.0);

        // Дата новости
        DateTimeFormatter originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter newFormat = DateTimeFormatter.ofPattern("d/MM/yy");
        LocalDateTime dateTime = LocalDateTime.parse(news.getDate_of_creation(), originalFormat);
        String formattedDate = dateTime.format(newFormat);

        Label dateLabel = new Label(formattedDate);

        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #90A4AE;");
        card.getChildren().add(dateLabel);
        AnchorPane.setTopAnchor(dateLabel, 15.0);
        AnchorPane.setRightAnchor(dateLabel, 20.0);


        // Краткое содержание
        Label contentLabel = new Label(news.getContent());
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #B0BEC5;");
        contentLabel.setPrefWidth(card.getPrefWidth() - 40);
        contentLabel.setPrefHeight(80);
        contentLabel.setWrapText(true);
        contentLabel.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        card.getChildren().add(contentLabel);
        AnchorPane.setTopAnchor(contentLabel, 50.0);
        AnchorPane.setLeftAnchor(contentLabel, 20.0);

        return card;
    }

    private UserModel initUser(String userData) {
        if (userData != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(userData);

                String id = jsonNode.get("id").asText();
                String login = jsonNode.get("login").asText();
                String email = jsonNode.get("email").asText();
                String username = jsonNode.get("username").asText();
                String avatar = jsonNode.get("avatar").asText();
                boolean admin = jsonNode.get("admin").asBoolean();

                return new UserModel(id, username, email, login, admin, avatar);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

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

    @FXML
    private void handleHomePageClick() {
        try {
            Stage currentStage = (Stage) btn_homepage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getMainPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private void handleProfilePageClick() {
        try {
            Stage currentStage = (Stage) btn_profilepage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getProfilePagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private void handleRefreshNewsClick() {
        try {
            loadNews();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleSubjectPageClick() {
        try {
            Stage currentStage = (Stage) btn_subjectpage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getSubjectsPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTeachersPageClick() {
        try {
            Stage currentStage = (Stage) btn_teacherspage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getTeachersPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private void handleSettingsPageClick() {
        try {
            Stage currentStage = (Stage) btn_subjectpage.getScene().getWindow();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getSettingsPagePath())));

            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load main page: " + e.getMessage());
            showErrorDialog("Error loading the application. Please try again later.");
        }
    }

    @FXML
    private MFXButton logoutbtn;
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
    private FontAwesomeIcon refresh_news;
    @FXML
    private Label news_upd_text;
    @FXML
    private Label utils_text;
    @FXML
    private Label descriptFIITDISCORD;
    @FXML
    private Label descriptFXcom;
    @FXML
    private Label descriptMladost;
    @FXML
    private Label descriptFIITTelegram;
    @FXML
    private ComboBox<String> languageComboBox;

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

        languageComboBox.setPromptText(languageBundle.getString("language.combobox"));
    }

    @FXML
    private Label navi_login_text;
    @FXML
    private Label navi_username_text;
    @FXML
    private ImageView navi_avatar;
}