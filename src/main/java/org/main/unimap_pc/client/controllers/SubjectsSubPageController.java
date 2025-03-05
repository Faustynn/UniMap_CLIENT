package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.main.unimap_pc.client.models.*;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class SubjectsSubPageController implements LanguageSupport {
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
            System.out.println("Subject is not null");
            System.out.println("Subject Code: " + subject.getCode());
            System.out.println("Subject Name: " + subject.getName());
            System.out.println("Subject Type: " + subject.getType());
            System.out.println("Subject Credits: " + subject.getCredits());
            System.out.println("Subject Study Type: " + subject.getStudyType());
            System.out.println("Subject Semester: " + subject.getSemester());
            System.out.println("Subject Languages: " + subject.getLanguages());
            System.out.println("Subject Completion Type: " + subject.getCompletionType());
            System.out.println("Subject Student Count: " + subject.getStudentCount());
            System.out.println("Subject Evaluation: " + subject.getEvaluation());
            System.out.println("Subject Assesment Methods: " + subject.getAssesmentMethods());
            System.out.println("Subject Learning Outcomes: " + subject.getLearningOutcomes());
            System.out.println("Subject Course Contents: " + subject.getCourseContents());
            System.out.println("Subject Planned Activities: " + subject.getPlannedActivities());
            System.out.println("Subject Evaluation Methods: " + subject.getEvaluationMethods());
            System.out.println("Subject A: " + subject.getAScore());
            System.out.println("Subject B: " + subject.getBScore());
            System.out.println("Subject C: " + subject.getCScore());
            System.out.println("Subject D: " + subject.getDScore());
            System.out.println("Subject E: " + subject.getEScore());
            System.out.println("Subject FX: " + subject.getFxScore());

            updateContent(subject);

            System.out.println(subject_assesmentMethods.getText());
            System.out.println(subject_evaluationMethods.getText());
            System.out.println(subject_assesmentMethods.getText());

            Platform.runLater(this::display_details);
        }else {
            System.out.println("Subject is null");
        }
    }
    @FXML
    private void initialize() {
        LanguageManager.getInstance().registerController(this);
        LanguageManager.changeLanguage(PreferenceServise.get("LANGUAGE").toString());
        updateUILanguage(LanguageManager.getCurrentBundle());

        dragArea.setOnMousePressed(this::handleMousePressed);
        dragArea.setOnMouseDragged(this::handleMouseDragged);
    }

    public void updateContent(Subject subject) {
        if (subject_code != null)
            subject_code.setText(subject.getCode());

        if (subject_abbr != null)
            subject_abbr.setText(subject.getName());

        if (subject_Type != null)
            subject_Type.setText(subject.getType());

        if (subject_credits != null)
            subject_credits.setText(String.valueOf(subject.getCredits()));

        if (subject_studyType != null)
            subject_studyType.setText(subject.getStudyType());

        if (subject_semester != null)
            subject_semester.setText(subject.getSemester());

        if (subject_languages != null)
            subject_languages.setText(subject.getLanguages().toString().replace("{", "").replace("}", "").replace("[", "").replace("]", ""));

        if (subject_completionType != null) {
            subject_completionType.setText(subject.getCompletionType());
        }

        if (subject_studentCount != null)
            subject_studentCount.setText(String.valueOf(subject.getStudentCount()));

        if (subject_evaluation != null)
            subject_evaluation.setText(subject.getEvaluation());

        if (subject_assesmentMethods != null)
            subject_assesmentMethods.setText(subject.getAssesmentMethods());

        if (subject_learnoutcomes != null)
            subject_learnoutcomes.setText(subject.getLearningOutcomes());

        if (subject_courseContents != null)
            subject_courseContents.setText(subject.getCourseContents());


        if (subject_plannedActivities != null)
            subject_plannedActivities.setText(subject.getPlannedActivities());

        if (subject_evaluationMethods != null)
            subject_evaluationMethods.setText(subject.getEvaluationMethods());

        if (subject_A != null)
            subject_A.setText(subject.getAScore()+"%");

        if (subject_B != null)
            subject_B.setText(subject.getBScore()+"%");

        if (subject_C != null)
            subject_C.setText(subject.getCScore()+"%");

        if (subject_D != null)
            subject_D.setText(subject.getDScore()+"%");

        if (subject_E != null)
            subject_E.setText(subject.getEScore()+"%");

        if (subject_FX != null)
            subject_FX.setText(subject.getFxScore()+"%");

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

        Map<String, DetailEntry> detailsMap = new LinkedHashMap<>() {{
            put("Teacher", new DetailEntry(
                    () -> subject_teacher_text != null ? subject_teacher_text.getText() : "Teacher",
                    () -> subject_teacher != null ? subject_teacher.getText() : "No teacher information available",
                    subject.getTeachers().toString()
            ));
            put("Evaluation", new DetailEntry(
                    () -> subject_evaluation_text != null ? subject_evaluation_text.getText() : "Evaluation",
                    () -> subject_evaluation != null ? subject_evaluation.getText() : "No evaluation information available",
                    subject.getEvaluation()
            ));
            put("Assessment Methods", new DetailEntry(
                    () -> subject_assesmentMethods_text != null ? subject_assesmentMethods_text.getText() : "Assessment Methods",
                    () -> subject_assesmentMethods != null ? subject_assesmentMethods.getText() : "No assessment methods information available",
                    subject.getAssesmentMethods()
            ));
            put("Evaluation Methods", new DetailEntry(
                    () -> subject_evaluationMethods_text != null ? subject_evaluationMethods_text.getText() : "Evaluation Methods",
                    () -> subject_evaluationMethods != null ? subject_evaluationMethods.getText() : "No evaluation methods information available",
                    subject.getEvaluationMethods()
            ));
            put("Planned Activities", new DetailEntry(
                    () -> subject_plannedActivities_text != null ? subject_plannedActivities_text.getText() : "Planned Activities",
                    () -> subject_plannedActivities != null ? subject_plannedActivities.getText() : "No planned activities information available",
                    subject.getPlannedActivities()
            ));
            put("Learning Outcomes", new DetailEntry(
                    () -> subject_learnoutcomes_text != null ? subject_learnoutcomes_text.getText() : "Learning Outcomes",
                    () -> subject_learnoutcomes != null ? subject_learnoutcomes.getText() : "No learning outcomes information available",
                    subject.getLearningOutcomes()
            ));
            put("Course Contents", new DetailEntry(
                    () -> subject_courseContents_text != null ? subject_courseContents_text.getText() : "Course Contents",
                    () -> subject_courseContents != null ? subject_courseContents.getText() : "No course contents information available",
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

        return "No information available for this section";
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
        AnchorPane.setTopAnchor(contentLabel, 30.0);
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
    public void handleСomments_button(){

    }

}