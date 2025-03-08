package org.main.unimap_pc.client.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
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
import org.main.unimap_pc.client.models.Teacher;
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
import java.util.HashMap;
import java.util.Map;

import static org.main.unimap_pc.client.controllers.LogInController.showErrorDialog;

public class TeachersPageController implements LanguageSupport {

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
    private Label navi_username_text, navi_login_text,ais_id_teach,name_teach,room_teach,teach_list;
    @FXML
    private ImageView navi_avatar;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private MFXButton logoutbtn, btn_homepage, btn_profilepage, btn_subjectpage, btn_teacherspage, btn_settingspage;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private AnchorPane anchorScrollPane;
    @FXML
    private ComboBox<String> roleCombo;
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
                updateUILanguage(LanguageManager.getCurrentBundle());
            } catch (Exception e) {
                Logger.error("Error changing language: " + e.getMessage());
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
        room_teach.setText(languageBundle.getString("room"));
        teach_list.setText(languageBundle.getString("teachers"));

        if (noResultsLabel != null) {
            noResultsLabel.setText(languageBundle.getString("criteria_teachers"));
        }
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
        FilterService.teacherSearchForm searchForm = new FilterService.teacherSearchForm(searchText,roleEnum,false);
        List<Teacher> filteredTeachers = filterService.filterTeachers(searchForm);

        System.out.println("Filtered teachers: " + filteredTeachers.size());
        for (Teacher teacher : filteredTeachers) {
            System.out.println("Teacher: " + teacher.getName());
        }

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
        VBox teacherContainer = new VBox(5);
        teacherContainer.setPrefWidth(anchorScrollPane.getPrefWidth());
        VBox.setVgrow(teacherContainer, Priority.ALWAYS);

        // Если список пуст - показываем сообщение
        if (teachers.isEmpty()) {
            ResourceBundle languageBundle = LanguageManager.getCurrentBundle();
            noResultsLabel = new Label(languageBundle.getString("criteria_subjects"));
            noResultsLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-alignment: center;");
            teacherContainer.getChildren().add(noResultsLabel);
        } else {
            // Create a map to store unique teachers by their ID
            Map<String, Teacher> uniqueTeachers = new HashMap<>();
            for (Teacher teacher : teachers) {
                if (uniqueTeachers.containsKey(teacher.getId())) {
                    // If the teacher already exists, merge the subjects
                    Teacher existingTeacher = uniqueTeachers.get(teacher.getId());
                    existingTeacher.getSubjects().addAll(teacher.getSubjects());
                } else {
                    // If the teacher is new, add them to the map
                    uniqueTeachers.put(teacher.getId(), teacher);
                }

    }

    // Create a card for each unique teacher and add it to the container
    for (Teacher teacher : uniqueTeachers.values()) {
        AnchorPane teacherCard = createTeacherCard(teacher);
        teacherContainer.getChildren().add(teacherCard);
    }
}

        anchorScrollPane.setStyle("-fx-background-color: #191C22;");
        anchorScrollPane.getChildren().add(teacherContainer);
        anchorScrollPane.setPrefHeight(teachers.size()*(50+8));
        anchorScrollPane.setMinHeight(350);
    }

    private AnchorPane createTeacherCard(Teacher teacher) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(50);
        card.setPrefWidth(anchorScrollPane.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: #2f3541;");

        // AIS ID
        Label abbreviationLabel = new Label(teacher.getId());
        abbreviationLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        card.getChildren().add(abbreviationLabel);
        AnchorPane.setTopAnchor(abbreviationLabel, 15.0);
        AnchorPane.setLeftAnchor(abbreviationLabel, 10.0);

        // Name and Surname
        String name = teacher.getName();
        if (name != null && name.length() > 120) {
            name = name.substring(0, 120) + "...";
        }
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 15.0);
        AnchorPane.setLeftAnchor(nameLabel, 120.0);

        // Room
        Label semesterLabel = new Label(teacher.getOffice());
        semesterLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        card.getChildren().add(semesterLabel);
        AnchorPane.setTopAnchor(semesterLabel, 15.0);
        AnchorPane.setLeftAnchor(semesterLabel, 675.0);

        // Добавляем обработчик клика
        card.setOnMouseClicked(event -> openTeacherPage(teacher));

        return card;
    }


    // Modal Window logic
    @FXML
    private void openModalWindow(String fxmlPath, String windowTitle, String errorMessage, Teacher teacher) {
        try {
            Stage parentStage = (Stage) (windowTitle.equals("TeacherPage") ? btn_teacherspage : btn_settingspage).getScene().getWindow();

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
                TeacherSubPageController controller = loader.getController();
                controller.setTeacher_entity(teacher);

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
                Logger.error("Failed to load FXML from path: " + fxmlPath);
                showErrorDialog(errorMessage + ": " + e.getMessage());
            }
        } catch (Exception e) {
            Logger.error("Unexpected error in openModalWindow" + e.getMessage());
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
    private void openTeacherPage(Teacher teacher) {
        openModalWindow(
                AppConfig.getTeachersSubPagePath(),
                "Teacher: " + teacher.getId(),
                "Error loading the forgot password window",
                teacher
        );
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
    private void handleLogout() throws IOException {
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
