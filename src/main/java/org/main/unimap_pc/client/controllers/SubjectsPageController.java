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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.UserService;
import javafx.scene.image.ImageView;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class SubjectsPageController implements LanguageSupport {
    @FXML
    private Label navi_username_text;
    @FXML
    private Label navi_login_text;
    @FXML
    private ImageView navi_avatar;

    @FXML
    private MFXComboBox languageComboBox;

    private String defLang;
    private String accessToken;

    @FXML
    private void initialize() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        loadCurrentLanguage();

        setupFilters();

        accessToken=UserService.getInstance().getAccessToken();
        defLang=UserService.getInstance().getDefLang();
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
    private Label subj_list;
    @FXML
    private Label abreviature;
    @FXML
    private Label name_code;
    @FXML
    private Label garant;
    @FXML
    private Label student_amount;
    @FXML
    private Label study_level_text;
    @FXML
    private Label subject_type_text;
    @FXML
    private Label semester_text;
    @FXML
    private Label filter_subject_text;




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


        applyFiltersBtn.setText(languageBundle.getString("apply.filters"));
        resetFiltersBtn.setText(languageBundle.getString("reset.filters"));
        searchField.setPromptText(languageBundle.getString("search"));
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



    @FXML
    private MFXTextField searchField;
    @FXML
    private MFXComboBox<String> subjectTypeCombo;
    @FXML
    private MFXComboBox<String> studyLevelCombo;
    @FXML
    private MFXComboBox<String> semesterCombo;
    @FXML
    private MFXButton applyFiltersBtn;
    @FXML
    private MFXButton resetFiltersBtn;

    private void setupFilters() {
        subjectTypeCombo.setValue("All Types");
        studyLevelCombo.setValue("All Levels");
        semesterCombo.setValue("All Semesters");

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 2 || newValue.isEmpty()) {
                applyFilters();
            }
        });

        applyFiltersBtn.setOnAction(event -> applyFilters());
        resetFiltersBtn.setOnAction(event -> resetFilters());
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String subjectType = subjectTypeCombo.getValue();
        String studyLevel = studyLevelCombo.getValue();
        String semester = semesterCombo.getValue();


     //    updateSubjectList(filteredSubjects);
    }
    private void resetFilters() {
        searchField.clear();
        subjectTypeCombo.setValue("All Types");
        studyLevelCombo.setValue("All Levels");
        semesterCombo.setValue("All Semesters");
        applyFilters();
    }


    private void updateSubjectList(List<Subject> subjects) {

    }

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorScrollPane;
    @FXML
    private Pane subjects_entity;
    @FXML
    private Label code_entity;
    @FXML
    private Label name_entity;
    @FXML
    private Label garant_entity;
    @FXML
    private Label students_entity;
}
