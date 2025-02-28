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
    @FXML private Label navi_username_text, navi_login_text, subj_list, abreviature, name_code, garant, student_amount, study_level_text, subject_type_text, semester_text, filter_subject_text;
    @FXML private ImageView navi_avatar;
    @FXML private MFXComboBox<String> languageComboBox, subjectTypeCombo, studyLevelCombo, semesterCombo;
    @FXML private MFXTextField searchField;
    @FXML private MFXButton logoutbtn, btn_homepage, btn_profilepage, btn_subjectpage, btn_teacherspage, btn_settingspage;
    @FXML private ScrollPane scrollPane;
    @FXML private AnchorPane anchorScrollPane;

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

        subjectTypeCombo.setOnAction(event -> {
            if (event.getSource() == subjectTypeCombo) {
                applyFilters();
            }
        });

        studyLevelCombo.setOnAction(event -> {
            if (event.getSource() == studyLevelCombo) {
                applyFilters();
            }
        });

        semesterCombo.setOnAction(event -> {
            if (event.getSource() == semesterCombo) {
                applyFilters();
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 2 || newVal.isEmpty()) applyFilters();
        });
    }
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String subjectType = subjectTypeCombo.getValue();
        String studyLevel = studyLevelCombo.getValue();
        String semester = semesterCombo.getValue();

        FilterService.subjectSearchForm.subjectTypeEnum subjectTypeEnum = switch (subjectType) {
            case "Povinny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV;
            case "Povinno Volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV_VOL;
            case "Volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.VOL;
            default -> FilterService.subjectSearchForm.subjectTypeEnum.NONE;
        };

        FilterService.subjectSearchForm.studyTypeEnum studyTypeEnum = switch (studyLevel) {
            case "Bacalaver" -> FilterService.subjectSearchForm.studyTypeEnum.BC;
            case "Ingeneer" -> FilterService.subjectSearchForm.studyTypeEnum.ING;
            default -> FilterService.subjectSearchForm.studyTypeEnum.NONE;
        };

        FilterService.subjectSearchForm.semesterEnum semesterEnum = switch (semester) {
            case "LS" -> FilterService.subjectSearchForm.semesterEnum.LS;
            case "ZS" -> FilterService.subjectSearchForm.semesterEnum.ZS;
            default -> FilterService.subjectSearchForm.semesterEnum.NONE;
        };

        FilterService filterService = new FilterService();
        FilterService.subjectSearchForm searchForm = new FilterService.subjectSearchForm(searchText, subjectTypeEnum, studyTypeEnum, semesterEnum);
        List<Subject> filteredSubjects = filterService.filterSubjects(searchForm);

        updateSubjectList(filteredSubjects);
        updateSelectedFiltersText();
    }

    private void updateSelectedFiltersText() {
    subjectTypeCombo.setStyle("-fx-text-fill: black;");
    semesterCombo.setStyle("-fx-text-fill: black;");
    studyLevelCombo.setStyle("-fx-text-fill: black;");
}


    private void updateSubjectList(List<Subject> subjects) {

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