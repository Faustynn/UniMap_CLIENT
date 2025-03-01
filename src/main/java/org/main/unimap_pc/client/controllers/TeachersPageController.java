package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.models.Teacher;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.FilterService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class TeachersPageController implements LanguageSupport {
    @FXML
    private Label navi_username_text, navi_login_text,ais_id_teach,name_teach,status_teach,room_teach,teach_list,id_ais_entity,id_name,id_status,id_room;
    @FXML
    private ImageView navi_avatar;
    @FXML
    private MFXComboBox<String> languageComboBox;
    @FXML
    private MFXTextField searchField;
    @FXML
    private MFXButton logoutbtn, btn_homepage, btn_profilepage, btn_subjectpage, btn_teacherspage, btn_settingspage;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorScrollPane;
    @FXML
    private MFXComboBox<String> roleCombo;


    private String defLang;
    private String accessToken;

    @FXML
    private void initialize() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        loadCurrentLanguage();

        setupFilters();
        applyFilters();


        defLang = UserService.getInstance().getDefLang();
        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            navi_username_text.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatar()));
        }
        LanguageManager.changeLanguage(defLang);
        LanguageManager.getInstance().registerController(this);
        updateUILanguage(LanguageManager.getCurrentBundle());
    }
    private void loadCurrentLanguage() {
        languageComboBox.setValue(defLang);
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
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
        languageComboBox.setPromptText(languageBundle.getString("language.combobox"));
        searchField.setPromptText(languageBundle.getString("search"));

        ais_id_teach.setText(languageBundle.getString("ais_id"));
        name_teach.setText(languageBundle.getString("name"));
        status_teach.setText(languageBundle.getString("status"));
        room_teach.setText(languageBundle.getString("room"));
        teach_list.setText(languageBundle.getString("teachers"));
    }


    private void setupFilters() {
        roleCombo.getItems().setAll("All Roles", "garant", "cviciaci", "prednasajuci","skusajuci");
        roleCombo.setValue("All Roles");
        roleCombo.setOnAction(event -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    private void applyFilters() {
        String searchText = searchField.getText().trim();
        String role = roleCombo.getValue();

        if (searchText.isEmpty()) {
            searchText = "";
        }

        FilterService.teacherSearchForm.roleEnum roleEnum = switch (role) {
            case "garant" -> FilterService.teacherSearchForm.roleEnum.GARANT;
            case "cviciaci" -> FilterService.teacherSearchForm.roleEnum.CVICIACI;
            case "prednasajuci" -> FilterService.teacherSearchForm.roleEnum.PREDNASAJUCI;
            case "skusajuci" -> FilterService.teacherSearchForm.roleEnum.SKUSAJUCI;
            default -> FilterService.teacherSearchForm.roleEnum.NONE;
        };

        FilterService filterService = new FilterService();
        FilterService.teacherSearchForm searchForm = new FilterService.teacherSearchForm(searchText,roleEnum);
        List<Teacher> filteredTeachers = filterService.filterTeachers(searchForm);

        System.out.println("Filtered teachers: " + filteredTeachers.size());

        updateTeacherList(filteredTeachers);
        updateSelectedFiltersText();
    }


    private void updateSelectedFiltersText() {
        if (!searchField.getText().trim().isEmpty()) {
            searchField.setStyle("-fx-border-color: #1976D2;");
        } else {
            searchField.setStyle("");
        }

        if (!roleCombo.getValue().equals("All Semesters")) {
            roleCombo.setStyle("-fx-text-fill: #1976D2;");
        } else {
            roleCombo.setStyle("-fx-text-fill: black;");
        }
    }
    private void updateTeacherList(List<Teacher> teachers) {
        anchorScrollPane.getChildren().clear();


        // контейнер для списка предметов
        VBox teacherContainer = new VBox(10);
        teacherContainer.setPrefWidth(anchorScrollPane.getPrefWidth());

        // Если список пуст - показываем сообщение
        if (teachers.isEmpty()) {
            Label noResultsLabel = new Label("No teachers found matching your criteria");
            noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            teacherContainer.getChildren().add(noResultsLabel);
        } else {
            // Создаем карточку для каждого предмета
            for (int i = 0; i < teachers.size(); i++) {
                Teacher teacher = teachers.get(i);
                AnchorPane teacherCard = createTeacherCard(teacher, i);
                teacherContainer.getChildren().add(teacherCard);
            }
        }

        anchorScrollPane.getChildren().add(teacherContainer);
        AnchorPane.setTopAnchor(teacherContainer, 10.0);
        AnchorPane.setLeftAnchor(teacherContainer, 10.0);
        AnchorPane.setRightAnchor(teacherContainer, 10.0);
    }

    private AnchorPane createTeacherCard(Teacher teacher, int index) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(80);
        card.setPrefWidth(anchorScrollPane.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: " + (index % 2 == 0 ? "#f5f5f5" : "#ffffff") +
                "; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5;");

        // AIS ID
        Label abbreviationLabel = new Label(teacher.getId());
        abbreviationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        card.getChildren().add(abbreviationLabel);
        AnchorPane.setTopAnchor(abbreviationLabel, 10.0);
        AnchorPane.setLeftAnchor(abbreviationLabel, 10.0);

        // Name and Surname
        Label nameLabel = new Label(teacher.getName());
        nameLabel.setStyle("-fx-font-size: 14px;");
        nameLabel.setMaxWidth(300);
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 10.0);
        AnchorPane.setLeftAnchor(nameLabel, 120.0);

        // Status
        Label typeLabel = new Label(teacher.getPhone());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        card.getChildren().add(typeLabel);
        AnchorPane.setTopAnchor(typeLabel, 35.0);
        AnchorPane.setLeftAnchor(typeLabel, 120.0);

        // Room
        Label semesterLabel = new Label(teacher.getOffice());
        semesterLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        card.getChildren().add(semesterLabel);
        AnchorPane.setTopAnchor(semesterLabel, 55.0);
        AnchorPane.setLeftAnchor(semesterLabel, 120.0);

        return card;
    }







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
    private void handleLogout() throws IOException {
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
