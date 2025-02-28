package org.main.unimap_pc.client.controllers;

import java.io.IOException;
import java.util.Objects;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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
            System.out.println("Loaded News");
            languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
            loadCurrentLanguage();
            accessToken = PreferenceServise.get("ACCESS_TOKEN").toString();
            refreshToken = PreferenceServise.get("REFRESH_TOKEN").toString();
            userData = PreferenceServise.get("USER_DATA").toString();
            cachedLanguage = PreferenceServise.get("LANGUAGE").toString();
            UserService.getInstance().setAccessToken(accessToken);
            UserService.getInstance().setRefreshToken(refreshToken);
            UserService.getInstance().setDefLang(cachedLanguage);

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
    private MFXScrollPane scrollPane_news;
    @FXML
    private Pane pane_for_news;

    private void loadNews() {
        CompletableFuture<String> newsJsonFuture = DataFetcher.fetchNews();

        newsJsonFuture.thenAccept(newsJson -> {
            if (newsJson != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<NewsModel> newsList = objectMapper.readValue(newsJson, new TypeReference<List<NewsModel>>() {});
                    Platform.runLater(() -> {
                        displayNews(newsList);
                    });
                } catch (Exception e) {
                    System.err.println("Failed to parse news JSON: " + e.getMessage());
                }
            } else {
                System.err.println("Failed to load news.");
            }
        });

    }


    private void displayNews(List<NewsModel> newsList) {
        // Create a VBox to hold the news items
        VBox newsContainer = new VBox(10); // 10 pixels spacing between items
        newsContainer.setPadding(new Insets(10)); // Add padding around the VBox

        // Clear any existing content in the scroll pane
        scrollPane_news.setContent(newsContainer);

        for (NewsModel news : newsList) {
            AnchorPane newsItem = createNewsItem(news);
            newsContainer.getChildren().add(newsItem);
        }
    }

    private AnchorPane createNewsItem(NewsModel news) {
        AnchorPane newsItem = new AnchorPane();
        newsItem.setPrefWidth(pane_for_news.getPrefWidth() -20);
        newsItem.setStyle("-fx-background-color: #659ac9; -fx-background-radius: 25; -fx-border-color: #36436F; -fx-border-radius: 25; -fx-border-width: 2; -fx-border-style: solid;");

        Label titleLabel = new Label(news.getTitle());
        titleLabel.setLayoutX(20);
        titleLabel.setLayoutY(10);
        titleLabel.setTextFill(javafx.scene.paint.Color.WHITE);

        Label descriptionLabel = new Label(news.getContent());
        descriptionLabel.setLayoutX(20);
        descriptionLabel.setLayoutY(50);
        descriptionLabel.setPrefWidth(newsItem.getPrefWidth() - 40);
        descriptionLabel.setWrapText(true);

        newsItem.getChildren().addAll(titleLabel, descriptionLabel);
        return newsItem;
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

    @FXML
    private MFXToggleButton viewmode;
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
    private Label buycoffe;
    @FXML
    private Label news_upd_text;
    @FXML
    private Label utils_text;
    @FXML
    private Label support_text;
    @FXML
    private Label descriptFIITDISCORD;
    @FXML
    private Label descriptFXcom;
    @FXML
    private Label descriptMladost;
    @FXML
    private Label descriptFIITTelegram;
    @FXML
    private MFXComboBox languageComboBox;

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
        support_text.setText(languageBundle.getString("support"));
        descriptFIITDISCORD.setText(languageBundle.getString("descriptFIITDISCORD"));
        descriptFXcom.setText(languageBundle.getString("descriptFXcom"));
        descriptMladost.setText(languageBundle.getString("descriptMladost"));
        descriptFIITTelegram.setText(languageBundle.getString("descriptFIITTelegram"));
        buycoffe.setText(languageBundle.getString("buycoffe"));

        languageComboBox.setText(languageBundle.getString("language.combobox"));


        if (news_title != null) {
            news_title.setText(languageBundle.getString("news.title"));
        }
        if (news_descrip != null) {
            news_descrip.setText(languageBundle.getString("news.description"));
        }
    }


    @FXML
    private Label navi_login_text;
    @FXML
    private Label navi_username_text;
    @FXML
    private ImageView navi_avatar;
    @FXML
    private MFXButton navi_avatar_btn;
    @FXML
    private FontAwesomeIcon gitHubIcon;
    @FXML
    private FontAwesomeIcon TipsIcon;
    @FXML
    private AnchorPane news1;
    @FXML
    private Label news_title;
    @FXML
    private Label news_descrip;


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
    public void handleRefreshNewsClick() {
        try {
            loadNews();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            e.printStackTrace();
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
            Stage currentStage = (Stage) btn_subjectpage.getScene().getWindow();
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
}
