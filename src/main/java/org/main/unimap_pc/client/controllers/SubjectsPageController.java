package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.*;
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
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.FilterService;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class SubjectsPageController implements LanguageSupport {
    @FXML
    private Label navi_username_text, navi_login_text, subj_list, abreviature, name_code, garant, student_amount, study_level_text, subject_type_text, semester_text, filter_subject_text;
    @FXML
    private ImageView navi_avatar;
    @FXML
    private MFXComboBox<String> languageComboBox, subjectTypeCombo, studyLevelCombo, semesterCombo;
    @FXML
    private MFXTextField searchField;
    @FXML
    private MFXButton logoutbtn, btn_homepage, btn_profilepage, btn_subjectpage, btn_teacherspage, btn_settingspage;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorScrollPane;

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

    private void setupFilters() {
        subjectTypeCombo.getItems().setAll("All Types", "Povinny", "Povinno Volitelny", "Volitelny");
        studyLevelCombo.getItems().setAll("All Levels", "Bacalaver", "Ingeneer");
        semesterCombo.getItems().setAll("All Semesters", "ZS", "LS");

        subjectTypeCombo.setValue("All Types");
        studyLevelCombo.setValue("All Levels");
        semesterCombo.setValue("All Semesters");

        subjectTypeCombo.setOnAction(event -> applyFilters());
        studyLevelCombo.setOnAction(event -> applyFilters());
        semesterCombo.setOnAction(event -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    private void applyFilters() {
        String searchText = searchField.getText().trim();

        if (searchText.isEmpty()) {
            searchText = "";
        }

        String subjectType = subjectTypeCombo.getValue();
        String studyLevel = studyLevelCombo.getValue();
        String semester = semesterCombo.getValue();

        FilterService.subjectSearchForm.subjectTypeEnum subjectTypeEnum = switch (subjectType) {
            case "povinny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV;
            case "povinno volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV_VOL;
            case "volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.VOL;
            default -> FilterService.subjectSearchForm.subjectTypeEnum.NONE;
        };

        FilterService.subjectSearchForm.studyTypeEnum studyTypeEnum = switch (studyLevel) {
            case "bacalaver" -> FilterService.subjectSearchForm.studyTypeEnum.BC;
            case "ingeneer" -> FilterService.subjectSearchForm.studyTypeEnum.ING;
            default -> FilterService.subjectSearchForm.studyTypeEnum.NONE;
        };

        FilterService.subjectSearchForm.semesterEnum semesterEnum = switch (semester) {
            case "ls" -> FilterService.subjectSearchForm.semesterEnum.LS;
            case "zs" -> FilterService.subjectSearchForm.semesterEnum.ZS;
            default -> FilterService.subjectSearchForm.semesterEnum.NONE;
        };

        FilterService filterService = new FilterService();
        FilterService.subjectSearchForm searchForm = new FilterService.subjectSearchForm(searchText, subjectTypeEnum, studyTypeEnum, semesterEnum);
        List<Subject> filteredSubjects = filterService.filterSubjects(searchForm);

        System.out.println("Filtered subjects: " + filteredSubjects.size());

        updateSubjectList(filteredSubjects);
        updateSelectedFiltersText();
    }

    private void updateSelectedFiltersText() {
        if (!subjectTypeCombo.getValue().equals("All Types")) {
            subjectTypeCombo.setStyle("-fx-text-fill: #1976D2;");
        } else {
            subjectTypeCombo.setStyle("-fx-text-fill: black;");
        }

        if (!semesterCombo.getValue().equals("All Semesters")) {
            semesterCombo.setStyle("-fx-text-fill: #1976D2;");
        } else {
            semesterCombo.setStyle("-fx-text-fill: black;");
        }

        if (!studyLevelCombo.getValue().equals("All Levels")) {
            studyLevelCombo.setStyle("-fx-text-fill: #1976D2;");
        } else {
            studyLevelCombo.setStyle("-fx-text-fill: black;");
        }

        if (!searchField.getText().trim().isEmpty()) {
            searchField.setStyle("-fx-border-color: #1976D2;");
        } else {
            searchField.setStyle("");
        }
    }
    private void updateSubjectList(List<Subject> subjects) {
        anchorScrollPane.getChildren().clear();


        // контейнер для списка предметов
        VBox subjectsContainer = new VBox(10);
        subjectsContainer.setPrefWidth(anchorScrollPane.getPrefWidth());

        // Если список пуст - показываем сообщение
        if (subjects.isEmpty()) {
            Label noResultsLabel = new Label("No subjects found matching your criteria");
            noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            subjectsContainer.getChildren().add(noResultsLabel);
        } else {
            // Создаем карточку для каждого предмета
            for (int i = 0; i < subjects.size(); i++) {
                Subject subject = subjects.get(i);
                AnchorPane subjectCard = createSubjectCard(subject, i);
                subjectsContainer.getChildren().add(subjectCard);
            }
        }

        anchorScrollPane.getChildren().add(subjectsContainer);
        AnchorPane.setTopAnchor(subjectsContainer, 10.0);
        AnchorPane.setLeftAnchor(subjectsContainer, 10.0);
        AnchorPane.setRightAnchor(subjectsContainer, 10.0);
    }

    private AnchorPane createSubjectCard(Subject subject, int index) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(80);
        card.setPrefWidth(anchorScrollPane.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: " + (index % 2 == 0 ? "#f5f5f5" : "#ffffff") +
                "; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5;");

        // Аббревиатура предмета
        Label abbreviationLabel = new Label(subject.getCode());
        abbreviationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        card.getChildren().add(abbreviationLabel);
        AnchorPane.setTopAnchor(abbreviationLabel, 10.0);
        AnchorPane.setLeftAnchor(abbreviationLabel, 10.0);

        // Название предмета
        Label nameLabel = new Label(subject.getName());
        nameLabel.setStyle("-fx-font-size: 14px;");
        nameLabel.setMaxWidth(300);
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 10.0);
        AnchorPane.setLeftAnchor(nameLabel, 120.0);

        // Тип предмета
        Label typeLabel = new Label(subject.getType());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        card.getChildren().add(typeLabel);
        AnchorPane.setTopAnchor(typeLabel, 35.0);
        AnchorPane.setLeftAnchor(typeLabel, 120.0);

        // Семестр
        Label semesterLabel = new Label("Semester: " + subject.getSemester());
        semesterLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575;");
        card.getChildren().add(semesterLabel);
        AnchorPane.setTopAnchor(semesterLabel, 55.0);
        AnchorPane.setLeftAnchor(semesterLabel, 120.0);

        // Гарант
        Label guarantorLabel = new Label("Guarantor: " + subject.getGarant());
        guarantorLabel.setStyle("-fx-font-size: 12px;");
        card.getChildren().add(guarantorLabel);
        AnchorPane.setTopAnchor(guarantorLabel, 35.0);
        AnchorPane.setRightAnchor(guarantorLabel, 10.0);

        // Количество студентов
        Label studentsLabel = new Label("Students: " + subject.getStudentCount());
        studentsLabel.setStyle("-fx-font-size: 12px;");
        card.getChildren().add(studentsLabel);
        AnchorPane.setTopAnchor(studentsLabel, 55.0);
        AnchorPane.setRightAnchor(studentsLabel, 10.0);

        return card;
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

        subj_list.setText(languageBundle.getString("subject.list"));
        abreviature.setText(languageBundle.getString("abreviature"));
        name_code.setText(languageBundle.getString("name.code"));
        garant.setText(languageBundle.getString("garant"));
        student_amount.setText(languageBundle.getString("student.amount"));
        study_level_text.setText(languageBundle.getString("study.level"));
        subject_type_text.setText(languageBundle.getString("subject.type"));
        semester_text.setText(languageBundle.getString("semester"));
        filter_subject_text.setText(languageBundle.getString("filter.subject"));

        searchField.setPromptText(languageBundle.getString("search"));
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
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        // Clear the user data
        Preferences prefs = Preferences.userNodeForPackage(HomePageController.class);
        prefs.remove("ACCESS_TOKEN");
        prefs.remove("REFRESH_TOKEN");
        prefs.remove("USER_DATA");
        prefs.remove("SUBJECTS");
        prefs.remove("TEACHERS");

        // Change scene to login
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLoginPagePath())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }
}