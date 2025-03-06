package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.*;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class SubjectsSubPageController implements LanguageSupport {
    @FXML
    public Label navi_username_text,navi_login_text;
    @FXML
    public ImageView navi_avatar;
    @FXML
    public Button comments_button;
    private Subject subject;
    @FXML
    private FontAwesomeIcon closeApp;
    @FXML
    private Label subject_A_text,subject_B_text,subject_C_text,subject_D_text,subject_E_text,subject_FX_text,subject_studentCount_text,subject_Type_text,subject_credits_text,subject_studyType_text,subject_semester_text,subject_languages_text,subject_completionType_text;
    @FXML
    private Label subject_A,subject_B,subject_C,subject_D,subject_E,subject_FX,subject_code,subject_abbr,subject_studentCount,subject_Type,subject_credits,subject_studyType,subject_semester,subject_languages,subject_completionType;
    @FXML
    private AnchorPane dragArea;
    @FXML
    private Label subject_teacher,subject_evaluation,subject_assesmentMethods,subject_evaluationMethods,subject_plannedActivities,subject_learnoutcomes,subject_courseContents;
    @FXML
    private Label subject_teacher_text,subject_evaluation_text,subject_assesmentMethods_text,subject_evaluationMethods_text,subject_plannedActivities_text,subject_learnoutcomes_text,subject_courseContents_text;
    @FXML
    private ScrollPane scroll_pane;

    @FXML
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }

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

    public void setSubject(Subject subject) {
        this.subject = subject;


        if (subject != null) {
            initializeLabels();

            //   printSubjectDetails(subject);

            updateContent(subject);
         //   printSubjectDetails(subject);
            Platform.runLater(this::display_details);
        } else {
            System.out.println("Subject is null");
        }
    }
    private void initializeLabels() {
        if (subject_evaluation == null)
            subject_evaluation = new Label();

        if (subject_assesmentMethods == null)
            subject_assesmentMethods = new Label();

        if (subject_learnoutcomes == null)
            subject_learnoutcomes = new Label();

        if (subject_courseContents == null)
            subject_courseContents = new Label();

        if (subject_plannedActivities == null)
            subject_plannedActivities = new Label();

        if (subject_evaluationMethods == null)
            subject_evaluationMethods = new Label();
    }

//    private void printSubjectDetails(Subject subject) {
//        System.out.println("Subject Details:");
//        System.out.println("Evaluation: " + subject.getEvaluation());
//        System.out.println("Assessment Methods: " + subject.getAssesmentMethods());
//        System.out.println("Learning Outcomes: " + subject.getLearningOutcomes());
//        System.out.println("Course Contents: " + subject.getCourseContents());
//        System.out.println("Planned Activities: " + subject.getPlannedActivities());
//        System.out.println("Evaluation Methods: " + subject.getEvaluationMethods());
//    }


    @FXML
    private void initialize() {
        LanguageManager.getInstance().registerController(this);
        LanguageManager.changeLanguage(PreferenceServise.get("LANGUAGE").toString());
        updateUILanguage(LanguageManager.getCurrentBundle());

        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            UserService.getInstance().setCurrentUser(user);
            navi_username_text.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatar()));
        }

        dragArea.setOnMousePressed(this::handleMousePressed);
        dragArea.setOnMouseDragged(this::handleMouseDragged);
    }

    private void updateContent(Subject subject) {
        if (subject_code != null) {
            subject_code.setText(
                    subject.getCode() != null
                            ? subject.getCode().replace("\\n", "\n")
                            : "Код предмета не указан"
            );
        } else {
            Logger.error("subject_code JavaFX element is not initialized!");
            System.err.println("subject_code JavaFX element is not initialized!");
        }

        if (subject_abbr != null) {
            subject_abbr.setText(
                    subject.getName() != null
                            ? subject.getName().replace("\\n", "\n")
                            : "Название предмета не указано"
            );
        } else {
            Logger.error("subject_abbr JavaFX element is not initialized!");
            System.err.println("subject_abbr JavaFX element is not initialized!");
        }

        if (subject_Type != null) {
            subject_Type.setText(
                    subject.getType() != null
                            ? subject.getType().replace("\\n", "\n")
                            : "Тип предмета не указан"
            );
        } else {
            Logger.error("subject_Type JavaFX element is not initialized!");
            System.err.println("subject_Type JavaFX element is not initialized!");
        }

        if (subject_credits != null) {
            subject_credits.setText(
                    subject.getCredits() != 0
                            ? String.valueOf(subject.getCredits()).replace("\\n", "\n")
                            : "Кредиты не указаны"
            );
        } else {
            Logger.error("subject_credits JavaFX element is not initialized!");
            System.err.println("subject_credits JavaFX element is not initialized!");
        }

        if (subject_studyType != null) {
            subject_studyType.setText(
                    subject.getStudyType() != null
                            ? subject.getStudyType().replace("\\n", "\n")
                            : "Тип обучения не указан"
            );
        } else {
            Logger.error("subject_studyType JavaFX element is not initialized!");
            System.err.println("subject_studyType JavaFX element is not initialized!");
        }

        if (subject_semester != null) {
            subject_semester.setText(
                    subject.getSemester() != null
                            ? subject.getSemester().replace("\\n", "\n")
                            : "Семестр не указан"
            );
        } else {
            Logger.error("subject_semester JavaFX element is not initialized!");
            System.err.println("subject_semester JavaFX element is not initialized!");
        }

        if (subject_languages != null) {
            subject_languages.setText(
                    subject.getLanguages() != null
                            ? subject.getLanguages().toString().replace("{", "").replace("}", "").replace("[", "").replace("]", "").replace("\\n", "\n")
                            : "Языки не указаны"
            );
        } else {
            Logger.error("subject_languages JavaFX element is not initialized!");
            System.err.println("subject_languages JavaFX element is not initialized!");
        }

        if (subject_completionType != null) {
            subject_completionType.setText(
                    subject.getCompletionType() != null
                            ? subject.getCompletionType().replace("\\n", "\n")
                            : "Тип завершения не указан"
            );
        } else {
            Logger.error("subject_completionType JavaFX element is not initialized!");
            System.err.println("subject_completionType JavaFX element is not initialized!");
        }

        if (subject_studentCount != null) {
            subject_studentCount.setText(
                    subject.getStudentCount() != 0
                            ? String.valueOf(subject.getStudentCount()).replace("\\n", "\n")
                            : "Количество студентов не указано"
            );
        } else {
            Logger.error("subject_studentCount JavaFX element is not initialized!");
            System.err.println("subject_studentCount JavaFX element is not initialized!");
        }

        if (subject_evaluation != null) {
            subject_evaluation.setText(
                    subject.getEvaluation() != null
                            ? subject.getEvaluation().replace("\\n", "\n")
                            : "Оценка не указана"
            );
        } else {
            Logger.error("subject_evaluation JavaFX element is not initialized!");
            System.err.println("subject_evaluation JavaFX element is not initialized!");
        }

        if (subject_assesmentMethods != null) {
            subject_assesmentMethods.setText(
                    subject.getAssesmentMethods() != null
                            ? subject.getAssesmentMethods().replace("\\n", "\n")
                            : "Методы оценки не указаны"
            );
        } else {
            Logger.error("subject_assesmentMethods JavaFX element is not initialized!");
            System.err.println("subject_assesmentMethods JavaFX element is not initialized!");
        }

        if (subject_learnoutcomes != null) {
            subject_learnoutcomes.setText(
                    subject.getLearningOutcomes() != null
                            ? subject.getLearningOutcomes().replace("\\n", "\n")
                            : "Результаты обучения не указаны"
            );
        } else {
            Logger.error("subject_learnoutcomes JavaFX element is not initialized!");
            System.err.println("subject_learnoutcomes JavaFX element is not initialized!");
        }

        if (subject_courseContents != null) {
            subject_courseContents.setText(
                    subject.getCourseContents() != null
                            ? subject.getCourseContents().replace("\\n", "\n")
                            : "Содержание курса не указано"
            );
        } else {
            Logger.error("subject_courseContents JavaFX element is not initialized!");
            System.err.println("subject_courseContents JavaFX element is not initialized!");
        }

        if (subject_plannedActivities != null) {
            subject_plannedActivities.setText(
                    subject.getPlannedActivities() != null
                            ? subject.getPlannedActivities().replace("\\n", "\n")
                            : "Планируемые мероприятия не указаны"
            );
        } else {
            Logger.error("subject_plannedActivities JavaFX element is not initialized!");
            System.err.println("subject_plannedActivities JavaFX element is not initialized!");
        }

        if (subject_evaluationMethods != null) {
            subject_evaluationMethods.setText(
                    subject.getEvaluationMethods() != null
                            ? subject.getEvaluationMethods().replace("\\n", "\n")
                            : "Методы оценки не указаны"
            );
        } else {
            Logger.error("subject_evaluationMethods JavaFX element is not initialized!");
            System.err.println("subject_evaluationMethods JavaFX element is not initialized!");
        }

        if (subject_A != null) {
            subject_A.setText(
                    subject.getAScore() != null
                            ? subject.getAScore() + "%".replace("\\n", "\n")
                            : "A оценка не указана"
            );
        } else {
            Logger.error("subject_A JavaFX element is not initialized!");
            System.err.println("subject_A JavaFX element is not initialized!");
        }

        if (subject_B != null) {
            subject_B.setText(
                    subject.getBScore() != null
                            ? subject.getBScore() + "%".replace("\\n", "\n")
                            : "B оценка не указана"
            );
        } else {
            Logger.error("subject_B JavaFX element is not initialized!");
            System.err.println("subject_B JavaFX element is not initialized!");
        }

        if (subject_C != null) {
            subject_C.setText(
                    subject.getCScore() != null
                            ? subject.getCScore() + "%".replace("\\n", "\n")
                            : "C оценка не указана"
            );
        } else {
            Logger.error("subject_C JavaFX element is not initialized!");
            System.err.println("subject_C JavaFX element is not initialized!");
        }

        if (subject_D != null) {
            subject_D.setText(
                    subject.getDScore() != null
                            ? subject.getDScore() + "%".replace("\\n", "\n")
                            : "D оценка не указана"
            );
        } else {
            Logger.error("subject_D JavaFX element is not initialized!");
            System.err.println("subject_D JavaFX element is not initialized!");
        }

        if (subject_E != null) {
            subject_E.setText(
                    subject.getEScore() != null
                            ? subject.getEScore() + "%".replace("\\n", "\n")
                            : "E оценка не указана"
            );
        } else {
            Logger.error("subject_E JavaFX element is not initialized!");
            System.err.println("subject_E JavaFX element is not initialized!");
        }

        if (subject_FX != null) {
            subject_FX.setText(
                    subject.getFxScore() != null
                            ? subject.getFxScore() + "%".replace("\\n", "\n")
                            : "FX оценка не указана"
            );
        } else {
            Logger.error("subject_FX JavaFX element is not initialized!");
            System.err.println("subject_FX JavaFX element is not initialized!");
        }
    }
    public void updateUILanguage(ResourceBundle languageBundle) {
            if (subject_teacher_text != null)
                subject_teacher_text.setText(languageBundle.getString("subject_teacher_text"));

            if (subject_Type_text != null)
                subject_Type_text.setText(languageBundle.getString("subject_Type_text"));

            if (subject_credits_text != null)
                subject_credits_text.setText(languageBundle.getString("subject_credits_text"));

            if (subject_studyType_text != null)
                subject_studyType_text.setText(languageBundle.getString("subject_studyType_text"));

            if (subject_semester_text != null)
                subject_semester_text.setText(languageBundle.getString("subject_semester_text"));

            if (subject_languages_text != null)
                subject_languages_text.setText(languageBundle.getString("subject_languages_text"));

            if (subject_completionType_text != null)
                subject_completionType_text.setText(languageBundle.getString("subject_completionType_text"));

            if (subject_studentCount_text != null)
                subject_studentCount_text.setText(languageBundle.getString("subject_studentCount_text"));

            if (subject_evaluation_text != null)
                subject_evaluation_text.setText(languageBundle.getString("subject_evaluation_text"));

            if (subject_assesmentMethods_text != null)
                subject_assesmentMethods_text.setText(languageBundle.getString("subject_assesmentMethods_text"));

            if (subject_learnoutcomes_text != null)
                subject_learnoutcomes_text.setText(languageBundle.getString("subject_learnoutcomes_text"));

            if (subject_courseContents_text != null)
                subject_courseContents_text.setText(languageBundle.getString("subject_courseContents_text"));

            if (subject_plannedActivities_text != null)
                subject_plannedActivities_text.setText(languageBundle.getString("subject_plannedActivities_text"));

            if (subject_evaluationMethods_text != null)
                subject_evaluationMethods_text.setText(languageBundle.getString("subject_evaluationMethods_text"));

            if (subject_A_text != null)
                subject_A_text.setText(languageBundle.getString("subject_A_text"));

            if (subject_B_text != null)
                subject_B_text.setText(languageBundle.getString("subject_B_text"));

            if (subject_C_text != null)
                subject_C_text.setText(languageBundle.getString("subject_C_text"));

            if (subject_D_text != null)
                subject_D_text.setText(languageBundle.getString("subject_D_text"));

            if (subject_E_text != null)
                subject_E_text.setText(languageBundle.getString("subject_E_text"));

            if (subject_FX_text != null)
                subject_FX_text.setText(languageBundle.getString("subject_FX_text"));

        }


    @FXML
    private AnchorPane subj_details_anchor;
    @FXML
private AnchorPane display_details() {
    subj_details_anchor.getChildren().clear();
    updateContent(subject);

    VBox modulesContainer = new VBox(5);
    ResourceBundle languageBundle = LanguageManager.getCurrentBundle();

    Map<String, DetailEntry> detailsMap = new LinkedHashMap<>() {{
        put("Teacher", new DetailEntry(
                () -> languageBundle.getString("subject_teacher_text"),
                () -> subject_teacher != null ? subject_teacher.getText() : languageBundle.getString("no_teacher_info"),
                subject.getTeachers().toString()
        ));
        put("Evaluation", new DetailEntry(
                () -> languageBundle.getString("subject_evaluation_text"),
                () -> subject_evaluation != null ? subject_evaluation.getText() : languageBundle.getString("no_evaluation_info"),
                subject.getEvaluation()
        ));
        put("Assessment Methods", new DetailEntry(
                () -> languageBundle.getString("subject_assesmentMethods_text"),
                () -> subject_assesmentMethods != null ? subject_assesmentMethods.getText() : languageBundle.getString("no_assessment_methods_info"),
                subject.getAssesmentMethods()
        ));
        put("Evaluation Methods", new DetailEntry(
                () -> languageBundle.getString("subject_evaluationMethods_text"),
                () -> subject_evaluationMethods != null ? subject_evaluationMethods.getText() : languageBundle.getString("no_evaluation_methods_info"),
                subject.getEvaluationMethods()
        ));
        put("Planned Activities", new DetailEntry(
                () -> languageBundle.getString("subject_plannedActivities_text"),
                () -> subject_plannedActivities != null ? subject_plannedActivities.getText() : languageBundle.getString("no_planned_activities_info"),
                subject.getPlannedActivities()
        ));
        put("Learning Outcomes", new DetailEntry(
                () -> languageBundle.getString("subject_learnoutcomes_text"),
                () -> subject_learnoutcomes != null ? subject_learnoutcomes.getText() : languageBundle.getString("no_learning_outcomes_info"),
                subject.getLearningOutcomes()
        ));
        put("Course Contents", new DetailEntry(
                () -> languageBundle.getString("subject_courseContents_text"),
                () -> subject_courseContents != null ? subject_courseContents.getText() : languageBundle.getString("no_course_contents_info"),
                subject.getCourseContents()
        ));
    }};

    // Process and add modules
    for (Map.Entry<String, DetailEntry> entry : detailsMap.entrySet()) {
        String moduleName = entry.getKey();
        DetailEntry detailEntry = entry.getValue();

        String title = detailEntry.titleSupplier.get();
        String content = determineContent(detailEntry);

        if (!content.contains("No information") || !content.trim().isEmpty()) {
            AnchorPane modulePane = createDetailModule(title, content, moduleName);
            modulesContainer.getChildren().add(modulePane);
        }
    }

    scroll_pane.setContent(modulesContainer);

    scroll_pane.setStyle(
            "-fx-background: transparent;" +
                    "-fx-background-color: transparent;" +
                    "-fx-control-inner-background: transparent;"
    );

    scroll_pane.getStyleClass().add("transparent-scroll-pane");
    scroll_pane.setFitToWidth(true);

    AnchorPane.setTopAnchor(scroll_pane, 0.0);
    AnchorPane.setBottomAnchor(scroll_pane, 0.0);
    AnchorPane.setLeftAnchor(scroll_pane, 0.0);
    AnchorPane.setRightAnchor(scroll_pane, 0.0);

    if (!subj_details_anchor.getChildren().contains(scroll_pane)) {
        subj_details_anchor.getChildren().add(scroll_pane);
    }

    return subj_details_anchor;
}
    private String determineContent(DetailEntry entry) {
        // Priority: 1. Label text, 2. Subject getter, 3. Default message
        if (entry.labelContentSupplier.get() != null && !entry.labelContentSupplier.get().trim().isEmpty()) {
            return entry.labelContentSupplier.get();
        }

        if (entry.subjectGetter != null && !entry.subjectGetter.trim().isEmpty()) {
            return entry.subjectGetter;
        }

        ResourceBundle languageBundle = LanguageManager.getCurrentBundle();
        String noInfoMessage = languageBundle.getString("no_information_available");

        return noInfoMessage;
    }

    // Inner class to hold detail entry information
    private static class DetailEntry {
        Supplier<String> titleSupplier;
        Supplier<String> labelContentSupplier;
        String subjectGetter;

        DetailEntry(Supplier<String> titleSupplier,
                    Supplier<String> labelContentSupplier,
                    String subjectGetter) {
            this.titleSupplier = titleSupplier;
            this.labelContentSupplier = labelContentSupplier;
            this.subjectGetter = subjectGetter;
        }
    }

    private AnchorPane createDetailModule(String title, String content, String moduleName) {
        AnchorPane modulePane = new AnchorPane();
        modulePane.setStyle(
                "-fx-background-color: #2F3541;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 15;"
        );

        // Title Label
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );
        modulePane.getChildren().add(titleLabel);
        AnchorPane.setTopAnchor(titleLabel, 0.0);
        AnchorPane.setLeftAnchor(titleLabel, 0.0);

        // Content Label
        Label contentLabel = new Label(content);
        contentLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: white;" +
                        "-fx-wrap-text: true;"
        );
        modulePane.getChildren().add(contentLabel);
        AnchorPane.setTopAnchor(contentLabel, 20.0);
        AnchorPane.setLeftAnchor(contentLabel, 0.0);
        AnchorPane.setRightAnchor(contentLabel, 0.0);

        // Dynamically adjust module height
        contentLabel.heightProperty().addListener((obs, oldVal, newVal) -> {
            modulePane.setMinHeight(Math.max(100, newVal.doubleValue() + 50));
        });

        modulePane.setPrefWidth(400);
        modulePane.getProperties().put("moduleName", moduleName);

        return modulePane;
    }

    @FXML
    public void handleCommentsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConfig.getCommentsPagePath()));
            Parent root = loader.load();

            CommentsPageController controller = loader.getController();
            controller.setDatas(1, subject.getCode());

            Stage currentStage = (Stage) comments_button.getScene().getWindow();
            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load Comments Page from Subjectsub page: " + e.getMessage());
        }
    }
}