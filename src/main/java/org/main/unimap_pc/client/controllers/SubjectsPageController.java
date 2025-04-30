package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

@Getter
@RequiredArgsConstructor
public class SubjectsPageController implements LanguageSupport {
    private static final String ALL_TYPES = "All Types";
    private static final String ALL_LEVELS = "All Levels";
    private static final String ALL_SEMESTERS = "All Semesters";
    private static final String ACTIVE_FILTER_STYLE = "-fx-text-fill: #1976D2;";
    private static final String DEFAULT_FILTER_STYLE = "-fx-text-fill: black;";
    private static final String CARD_STYLE = "-fx-background-color: #2f3541;";
    private static final String ERROR_LOADING_PAGE = "Error loading the application. Please try again later.";

    @FXML private AnchorPane dragArea;
    @FXML private Label navi_username_text, navi_login_text, subj_list, abreviature, name_code,
            garant, student_amount, study_level_text, subject_type_text,
            semester_text, filter_subject_text, semester, type;
    @FXML private ImageView navi_avatar;
    @FXML private ComboBox<String> languageComboBox, subjectTypeCombo, studyLevelCombo, semesterCombo;
    @FXML private TextField searchField;
    @FXML private MFXButton logoutbtn, btn_homepage, btn_profilepage, btn_subjectpage,
            btn_teacherspage, btn_settingspage;
    @FXML private ScrollPane scrollPane;
    @FXML private AnchorPane anchorScrollPane;

    private Label noResultsLabel;
    private String defaultLanguage;
    private final WindowDragHandler windowDragHandler = new WindowDragHandler();

    @FXML
    private void initialize() {
        setupLanguageSelector();
        displayUserInfo();
        setupFilters();
        initializeWindowDragging();
        applyFilters();
        setupWindowResizeListeners();
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
                System.out.println("Error changing language: " + e.getMessage());
                showErrorDialog("Failed to change language: " + e.getMessage());
            }
        });
    }

    private void displayUserInfo() {
        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            navi_username_text.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
        }
    }

    private void initializeWindowDragging() {
        windowDragHandler.setupWindowDragging(dragArea);
    }

    private void setupWindowResizeListeners() {
        Scene scene = dragArea.getScene();
        if (scene != null) {
            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                // TODO: Resize logic
            });
            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                // TODO: Resize logic
            });
        }
    }

    private void setupFilters() {
        // Initialize filter combos
        initializeFilterCombo(subjectTypeCombo, List.of(ALL_TYPES, "povinny", "povinne volitelny", "volitelny"));
        initializeFilterCombo(studyLevelCombo, List.of(ALL_LEVELS, "bakalarsky", "inziniersky"));
        initializeFilterCombo(semesterCombo, List.of(ALL_SEMESTERS, "ZS", "LS"));

        // Set filter change listeners
        subjectTypeCombo.setOnAction(event -> applyFilters());
        studyLevelCombo.setOnAction(event -> applyFilters());
        semesterCombo.setOnAction(event -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initializeFilterCombo(ComboBox<String> combo, List<String> items) {
        combo.getItems().setAll(items);
        combo.setValue(items.get(0));
    }

    private void applyFilters() {
        String searchText = searchField.getText().trim().isEmpty() ? "" : searchField.getText().trim();

        FilterService.subjectSearchForm.subjectTypeEnum subjectTypeEnum = getSubjectTypeEnum();
        FilterService.subjectSearchForm.studyTypeEnum studyTypeEnum = getStudyTypeEnum();
        FilterService.subjectSearchForm.semesterEnum semesterEnum = getSemesterEnum();

        FilterService.subjectSearchForm searchForm = new FilterService.subjectSearchForm(
                searchText, subjectTypeEnum, studyTypeEnum, semesterEnum
        );
        List<Subject> filteredSubjects = FilterService.filterSubjects(searchForm);
    //    System.out.println("Filtered subjects: " + filteredSubjects.size());

        updateSubjectList(filteredSubjects);
        updateSelectedFiltersText();
    }

    private FilterService.subjectSearchForm.subjectTypeEnum getSubjectTypeEnum() {
        return switch (subjectTypeCombo.getValue()) {
            case "povinny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV;
            case "povinne volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.POV_VOL;
            case "volitelny" -> FilterService.subjectSearchForm.subjectTypeEnum.VOL;
            default -> FilterService.subjectSearchForm.subjectTypeEnum.NONE;
        };
    }

    private FilterService.subjectSearchForm.studyTypeEnum getStudyTypeEnum() {
        return switch (studyLevelCombo.getValue()) {
            case "bakalarsky" -> FilterService.subjectSearchForm.studyTypeEnum.BC;
            case "inziniersky" -> FilterService.subjectSearchForm.studyTypeEnum.ING;
            default -> FilterService.subjectSearchForm.studyTypeEnum.NONE;
        };
    }

    private FilterService.subjectSearchForm.semesterEnum getSemesterEnum() {
        return switch (semesterCombo.getValue()) {
            case "LS" -> FilterService.subjectSearchForm.semesterEnum.LS;
            case "ZS" -> FilterService.subjectSearchForm.semesterEnum.ZS;
            default -> FilterService.subjectSearchForm.semesterEnum.NONE;
        };
    }

    private void updateSelectedFiltersText() {
        updateFilterStyle(subjectTypeCombo, ALL_TYPES);
        updateFilterStyle(semesterCombo, ALL_SEMESTERS);
        updateFilterStyle(studyLevelCombo, ALL_LEVELS);
    }

    private void updateFilterStyle(ComboBox<String> combo, String defaultValue) {
        combo.setStyle(combo.getValue().equals(defaultValue) ? DEFAULT_FILTER_STYLE : ACTIVE_FILTER_STYLE);
    }

    private void updateSubjectList(List<Subject> subjects) {
        anchorScrollPane.getChildren().clear();

        VBox subjectsContainer = createSubjectsContainer();

        if (subjects.isEmpty()) {
            addNoResultsLabel(subjectsContainer);
        } else {
            addSubjectCards(subjectsContainer, subjects);
        }

        anchorScrollPane.setStyle("-fx-background-color: #191C22;");
        anchorScrollPane.getChildren().add(subjectsContainer);
        anchorScrollPane.setPrefHeight(Math.max(300, subjects.size() * (50 + 8)));
        anchorScrollPane.setMinHeight(300);
    }

    private VBox createSubjectsContainer() {
        VBox container = new VBox(5);
        container.setPrefWidth(anchorScrollPane.getPrefWidth());
        VBox.setVgrow(container, Priority.ALWAYS);
        return container;
    }

    private void addNoResultsLabel(VBox container) {
        ResourceBundle languageBundle = LanguageManager.getCurrentBundle();
        noResultsLabel = new Label(languageBundle.getString("criteria_subjects"));
        noResultsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-alignment: center;");
        container.getChildren().add(noResultsLabel);
    }

    private void addSubjectCards(VBox container, List<Subject> subjects) {
        for (Subject subject : subjects) {
            AnchorPane subjectCard = createSubjectCard(subject);
            container.getChildren().add(subjectCard);
        }
    }

    private AnchorPane createSubjectCard(Subject subject) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(50);
        card.setPrefWidth(anchorScrollPane.getPrefWidth() - 20);
        card.setStyle(CARD_STYLE);

        // Add card elements
        addSubjectCode(card, subject.getCode());
        addSubjectName(card, subject.getName());
        addSubjectGuarantor(card, subject.getGarant());
        addSubjectSemester(card, subject.getSemester());
        addSubjectType(card, subject.getType());
        addStudentCount(card, subject.getStudentCount());

        // Add click handler
        card.setOnMouseClicked(event -> openSubjectSubPage(subject));

        return card;
    }

    private void addSubjectCode(AnchorPane card, String code) {
        Label codeLabel = new Label(code);
        codeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");
        card.getChildren().add(codeLabel);
        AnchorPane.setTopAnchor(codeLabel, 15.0);
        AnchorPane.setLeftAnchor(codeLabel, 10.0);
    }

    private void addSubjectName(AnchorPane card, String name) {
        String displayName = name.length() > 36 ? name.substring(0, 36) + "..." : name;
        Label nameLabel = new Label(displayName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        nameLabel.setMaxWidth(300);
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 15.0);
        AnchorPane.setLeftAnchor(nameLabel, 90.0);
    }

    private void addSubjectGuarantor(AnchorPane card, String guarantor) {
        String displayGuarantor = guarantor;
        if (guarantor != null && guarantor.length() > 30) {
            displayGuarantor = guarantor.substring(0, 30) + "...";
        }
        Label guarantorLabel = new Label(displayGuarantor);
        guarantorLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(guarantorLabel);
        AnchorPane.setTopAnchor(guarantorLabel, 15.0);
        AnchorPane.setLeftAnchor(guarantorLabel, 333.0);
    }

    private void addSubjectSemester(AnchorPane card, String semester) {
        Label semesterLabel = new Label(semester);
        semesterLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(semesterLabel);
        AnchorPane.setTopAnchor(semesterLabel, 15.0);
        AnchorPane.setRightAnchor(semesterLabel, 230.0);
    }

    private void addSubjectType(AnchorPane card, String type) {
        String displayType = type
                .replace("povinné-voliteľný", "PV")
                .replace("povinný", "P")
                .replace("voliteľný", "V");
        Label typeLabel = new Label(displayType);
        typeLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(typeLabel);
        AnchorPane.setTopAnchor(typeLabel, 15.0);
        AnchorPane.setRightAnchor(typeLabel, 145.0);
    }

    private void addStudentCount(AnchorPane card, long count) {
        Label studentsLabel = new Label(String.valueOf(count));
        studentsLabel.setStyle("-fx-text-fill: white;");
        card.getChildren().add(studentsLabel);
        AnchorPane.setTopAnchor(studentsLabel, 15.0);
        AnchorPane.setRightAnchor(studentsLabel, 80.0);
    }

    private void openSubjectSubPage(Subject subject) {
        openModalWindow(
                AppConfig.getSUBJECTS_SUB_PAGE_PATH(),
                "Subject: " + subject.getCode(),
                subject
        );
    }

    private void openModalWindow(String fxmlPath, String windowTitle, Subject subject) {
        try {
            Stage parentStage = (Stage) (windowTitle.startsWith("Subject") ?
                    btn_subjectpage : btn_settingspage).getScene().getWindow();

            if (getClass().getResource(fxmlPath) == null) {
                showErrorDialog("Resource not found: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (LanguageManager.getCurrentBundle() != null) {
                loader.setResources(LanguageManager.getCurrentBundle());
            }

            AnchorPane modalPane = loader.load();
            SubjectsSubPageController controller = loader.getController();
            controller.setSubject(subject);

            createAndShowModalStage(modalPane, windowTitle, parentStage);
        } catch (IOException e) {
            System.out.println("Failed to load FXML: " + e.getMessage());
            Logger.error("Failed to load FXML from path: " + fxmlPath);
            showErrorDialog("Error loading the subject details window" + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error in openModalWindow: " + e.getMessage());
            Logger.error("Unexpected error in openModalWindow");
            showErrorDialog("Error loading the subject details window" + ": " + e.getMessage());
        }
    }

    private void createAndShowModalStage(Parent modalPane, String windowTitle, Stage parentStage) {
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
    }

    private StackPane createOverlay(Stage parentStage) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        overlay.setPrefSize(parentStage.getWidth(), parentStage.getHeight());
        overlay.setOnMouseClicked(event -> Toolkit.getDefaultToolkit().beep());
        return overlay;
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
        type.setText(languageBundle.getString("type"));
        semester.setText(languageBundle.getString("semester"));

        searchField.setPromptText(languageBundle.getString("search"));

        if (noResultsLabel != null) {
            noResultsLabel.setText(languageBundle.getString("criteria_subjects"));
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
            System.out.println("Failed to load page: " + e.getMessage());
            Logger.error("Failed to load page: " + e.getMessage());
            showErrorDialog(ERROR_LOADING_PAGE);
        }
    }

    @FXML
    private void handleLogout() throws IOException {
        // Clear user data
        PreferenceServise.deletePreferences();
        PreferenceServise.put("REMEMBER", false);
        CacheService.clearCache();

        // Navigate to login page
        Stage stage = (Stage) logoutbtn.getScene().getWindow();
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLOGIN_PAGE_PATH())));
        Scene mainScene = new Scene(root);
        stage.setScene(mainScene);
        stage.show();
    }
}