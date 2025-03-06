package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.*;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.CacheService;
import org.main.unimap_pc.client.services.FilterService;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;
import javafx.scene.control.TextField;


import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class SubjectsPageController implements LanguageSupport {

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
    private Label navi_username_text, navi_login_text, subj_list, abreviature, name_code, garant, student_amount, study_level_text, subject_type_text, semester_text, filter_subject_text,semester,type;
    @FXML
    private ImageView navi_avatar;
    @FXML
    private ComboBox<String> languageComboBox, subjectTypeCombo, studyLevelCombo, semesterCombo;
    @FXML
    private TextField searchField;
    @FXML
    private MFXButton logoutbtn, btn_homepage, btn_profilepage, btn_subjectpage, btn_teacherspage, btn_settingspage, btnForgotPass, btnSignup;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorScrollPane;
    private Label noResultsLabel;

    private String defLang;
    private String accessToken;

    @FXML
    private void initialize() {
        languageComboBox.getItems().addAll("English", "Українська", "Slovenský");
        loadCurrentLanguage();

        setupFilters();
        applyFilters();

        defLang = PreferenceServise.get("LANGUAGE").toString();
        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            navi_username_text.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatar()));
        }
        LanguageManager.changeLanguage(defLang);
        LanguageManager.getInstance().registerController(this);
        updateUILanguage(LanguageManager.getCurrentBundle());

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
        languageComboBox.setValue(defLang);
        languageComboBox.setOnAction(event -> {
            try {
                String newLanguage = languageComboBox.getValue();
                String languageCode = AppConfig.getLANGUAGE_CODES().get(newLanguage);
                LanguageManager.changeLanguage(languageCode);
                PreferenceServise.put("LANGUAGE", languageCode);
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                Logger.error("Error changing language: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void setupFilters() {
        subjectTypeCombo.getItems().setAll("All Types", "povinny", "povinne volitelny", "volitelny");
        studyLevelCombo.getItems().setAll("All Levels", "bakalarsky", "inziniersky");
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
            case "povinne volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV_VOL;
            case "volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.VOL;
            default -> FilterService.subjectSearchForm.subjectTypeEnum.NONE;
        };

        FilterService.subjectSearchForm.studyTypeEnum studyTypeEnum = switch (studyLevel) {
            case "bakalarsky" -> FilterService.subjectSearchForm.studyTypeEnum.BC;
            case "inziniersky" -> FilterService.subjectSearchForm.studyTypeEnum.ING;
            default -> FilterService.subjectSearchForm.studyTypeEnum.NONE;
        };

        FilterService.subjectSearchForm.semesterEnum semesterEnum = switch (semester) {
            case "LS" -> FilterService.subjectSearchForm.semesterEnum.LS;
            case "ZS" -> FilterService.subjectSearchForm.semesterEnum.ZS;
            default -> FilterService.subjectSearchForm.semesterEnum.NONE;
        };

        FilterService filterService = new FilterService();
        FilterService.subjectSearchForm searchForm = new FilterService.subjectSearchForm(searchText, subjectTypeEnum, studyTypeEnum, semesterEnum);
        List<Subject> filteredSubjects = FilterService.filterSubjects(searchForm);
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

    }
    private void updateSubjectList(List<Subject> subjects) {
        anchorScrollPane.getChildren().clear();

        VBox subjectsContainer = new VBox(5);
        subjectsContainer.setPrefWidth(anchorScrollPane.getPrefWidth());
        VBox.setVgrow(subjectsContainer, Priority.ALWAYS);

        if (subjects.isEmpty()) {
            ResourceBundle languageBundle = LanguageManager.getCurrentBundle();
            noResultsLabel = new Label(languageBundle.getString("criteria_subjects"));
            noResultsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-alignment: center;");
            subjectsContainer.getChildren().add(noResultsLabel);
        } else {
            // Создаем карточку для каждого предмета
            for (int i = 0; i < subjects.size(); i++) {
                Subject subject = subjects.get(i);
                AnchorPane subjectCard = createSubjectCard(subject);
                subjectsContainer.getChildren().add(subjectCard);
            }
        }

        anchorScrollPane.setStyle("-fx-background-color: #191C22;");
        anchorScrollPane.getChildren().add(subjectsContainer);
        anchorScrollPane.setPrefHeight(subjects.size()*(50+8));
        anchorScrollPane.setMinHeight(300);
    }
    private AnchorPane createSubjectCard(Subject subject) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(50);
        card.setPrefWidth(anchorScrollPane.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: #2f3541;");

        // Аббревиатура предмета
        Label abbreviationLabel = new Label(subject.getCode());
        abbreviationLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

        card.getChildren().add(abbreviationLabel);
        AnchorPane.setTopAnchor(abbreviationLabel, 15.0);
        AnchorPane.setLeftAnchor(abbreviationLabel, 10.0);

        // Название предмета
        String name = subject.getName();
        if (name.length() > 36) {
            name = name.substring(0, 36) + "...";
        }

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        nameLabel.setMaxWidth(300);
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 15.0);
        AnchorPane.setLeftAnchor(nameLabel, 90.0);

        // Гарант
        String guarantor = subject.getGarant();
        if (guarantor != null && guarantor.length() > 30) {
            guarantor = guarantor.substring(0, 30) + "...";
        }
        Label guarantorLabel = new Label(guarantor);
        guarantorLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(guarantorLabel);
        AnchorPane.setTopAnchor(guarantorLabel, 15.0);
        AnchorPane.setLeftAnchor(guarantorLabel, 333.0);


        // Семестр
        Label semesterLabel = new Label(subject.getSemester());
        semesterLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(semesterLabel);
        AnchorPane.setTopAnchor(semesterLabel, 15.0);
        AnchorPane.setRightAnchor(semesterLabel, 230.0);

        // Тип предмета
        Label typeLabel = new Label(subject.getType().replace("povinne voliteľný", "PV").replace("povinný", "P").replace("voliteľný", "V"));
        typeLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(typeLabel);
        AnchorPane.setTopAnchor(typeLabel, 15.0);
        AnchorPane.setRightAnchor(typeLabel, 145.0);


        // Количество студентов
        Label studentsLabel = new Label(String.valueOf(subject.getStudentCount()));
        studentsLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(studentsLabel);
        AnchorPane.setTopAnchor(studentsLabel, 15.0);
        AnchorPane.setRightAnchor(studentsLabel, 80.0);
        // Добавляем обработчик клика
        card.setOnMouseClicked(event -> openSubjectSubPage(subject));

        return card;
    }


    // Modal Window logic
    @FXML
    private void openModalWindow(String fxmlPath, String windowTitle, String errorMessage, Subject subject) {
        try {
            Stage parentStage = (Stage) (windowTitle.equals("SubjectPage") ? btn_subjectpage : btn_settingspage).getScene().getWindow();

            if (getClass().getResource(fxmlPath) == null) {
                showErrorDialog("Resource not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (LanguageManager.getCurrentBundle() != null) {
                loader.setResources(LanguageManager.getCurrentBundle());
            }

            try {
                AnchorPane modalPane = loader.load();
                SubjectsSubPageController controller = loader.getController();
                controller.setSubject(subject);

                Scene modalScene = new Scene(modalPane);
                Stage modalStage = new Stage();

                modalStage.initStyle(StageStyle.TRANSPARENT);
                modalStage.initModality(Modality.WINDOW_MODAL);
                modalStage.initOwner(parentStage);
                modalStage.setTitle(windowTitle);

                modalStage.setScene(modalScene);

                StackPane overlay = createOverlay(parentStage);

                Scene parentScene = parentStage.getScene();
                AnchorPane parentRoot = (AnchorPane) parentScene.getRoot();
                parentRoot.getChildren().add(overlay);

                modalStage.setOnHidden(event -> parentRoot.getChildren().remove(overlay));

                modalStage.showAndWait();
            } catch (IOException e) {
                System.err.println("Failed to load FXML from path: " + fxmlPath);
                e.printStackTrace();
                showErrorDialog(errorMessage + ": " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in openModalWindow");
            e.printStackTrace();
            showErrorDialog(errorMessage + ": " + e.getMessage());
        }
    }
    private StackPane createOverlay(Stage parentStage) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setPrefSize(parentStage.getWidth(), parentStage.getHeight());

        overlay.setOnMouseClicked(event -> Toolkit.getDefaultToolkit().beep());

        return overlay;
    }
    private void openSubjectSubPage(Subject subject) {
        openModalWindow(
                AppConfig.getSubjectsSubPagePath(),
                "Subject: " + subject.getCode(),
                "Error loading the forgot password window",
                subject
        );
    }


    @Override
    public void updateUILanguage(ResourceBundle languageBundle) {
        logoutbtn.setText(languageBundle.getString("logout"));
        btn_homepage.setText(languageBundle.getString("homepage"));
        btn_profilepage.setText(languageBundle.getString("profilepage"));
        btn_subjectpage.setText(languageBundle.getString("subjectpage"));
        btn_teacherspage.setText(languageBundle.getString("teacherspage"));
        btn_settingspage.setText(languageBundle.getString("settingspage"));

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
        type.setText(languageBundle.getString("type"));
        semester.setText(languageBundle.getString("semester"));

        if (noResultsLabel != null) {
            noResultsLabel.setText(languageBundle.getString("criteria_subjects"));
        }
    }

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
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLoginPagePath())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }
}