package org.main.unimap_pc.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.services.DataFetcher;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.util.ResourceBundle;

public class SubjectsSubPageController implements LanguageSupport {
    @Setter
    @Getter
    private Subject subject;
    @FXML
    private Label closeApp,subject_A_text,subject_B_text,subject_C_text,subject_D_text,subject_E_text,subject_FX_text,subject_teacher_text,subject_evaluation_text,subject_assesmentMethods_text,subject_evaluationMethods_text,subject_plannedActivities_text,subject_code_text,subject_abbr_text,subject_studentCount_text,subject_Type_text,subject_credits_text,subject_studyType_text,subject_semester_text,subject_languages_text,subject_completionType_text,subject_learnoutcomes_text,subject_courseContents_text;
    @FXML
    private Label subject_A,subject_B,subject_C,subject_D,subject_E,subject_FX,subject_teacher,subject_evaluation,subject_assesmentMethods,subject_evaluationMethods,subject_plannedActivities,subject_code,subject_abbr,subject_studentCount,subject_Type,subject_credits,subject_studyType,subject_semester,subject_languages,subject_completionType,subject_learnoutcomes,subject_courseContents;
    @FXML
    private AnchorPane dragArea;

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
            updateContent(subject);
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
        if (subject == null) {
            return;
        }

        subject_code.setText(subject.getCode());
        subject_abbr.setText(subject.getName());
        subject_Type.setText(subject.getType());
        subject_credits.setText(String.valueOf(subject.getCredits()));
        subject_studyType.setText(subject.getStudyType());
        subject_semester.setText(subject.getSemester());
        subject_languages.setText(subject.getLanguages().toString().replace("{", "").replace("}", "").replace("[", "").replace("]", ""));
        subject_completionType.setText(subject.getCompletionType());        subject_completionType.setText(subject.getCompletionType());
        subject_studentCount.setText(String.valueOf(subject.getStudentCount()));
        subject_evaluation.setText(subject.getEvaluation());
        subject_assesmentMethods.setText(subject.getAssesmentMethods());
        subject_learnoutcomes.setText(subject.getLearningOutcomes());
        subject_courseContents.setText(subject.getCourseContents());
        subject_plannedActivities.setText(subject.getPlannedActivities());
        subject_evaluationMethods.setText(subject.getEvaluationMethods());

        subject_A.setText(subject.getAScore());
        subject_B.setText(subject.getBScore());
        subject_C.setText(subject.getCScore());
        subject_D.setText(subject.getDScore());
        subject_E.setText(subject.getEScore());
        subject_FX.setText(subject.getFxScore());
        subject_teacher.setText(String.valueOf(subject.getTeachers()));
    }
    public void updateUILanguage(ResourceBundle languageBundle) {
        subject_teacher_text.setText(languageBundle.getString("subject_teacher_text"));

        subject_code_text.setText(languageBundle.getString("subject_code_text"));
        subject_abbr_text.setText(languageBundle.getString("subject_abbr_text"));
        subject_Type_text.setText(languageBundle.getString("subject_Type_text"));
        subject_credits_text.setText(languageBundle.getString("subject_credits_text"));
        subject_studyType_text.setText(languageBundle.getString("subject_studyType_text"));
        subject_semester_text.setText(languageBundle.getString("subject_semester_text"));
        subject_languages_text.setText(languageBundle.getString("subject_languages_text"));
        subject_completionType_text.setText(languageBundle.getString("subject_completionType_text"));
        subject_studentCount_text.setText(languageBundle.getString("subject_studentCount_text"));
        subject_evaluation_text.setText(languageBundle.getString("subject_evaluation_text"));
        subject_assesmentMethods_text.setText(languageBundle.getString("subject_assesmentMethods_text"));
        subject_learnoutcomes_text.setText(languageBundle.getString("subject_learnoutcomes_text"));
        subject_courseContents_text.setText(languageBundle.getString("subject_courseContents_text"));
        subject_plannedActivities_text.setText(languageBundle.getString("subject_plannedActivities_text"));
        subject_evaluationMethods_text.setText(languageBundle.getString("subject_evaluationMethods_text"));

        subject_A_text.setText(languageBundle.getString("subject_A_text"));
        subject_B_text.setText(languageBundle.getString("subject_B_text"));
        subject_C_text.setText(languageBundle.getString("subject_C_text"));
        subject_D_text.setText(languageBundle.getString("subject_D_text"));
        subject_E_text.setText(languageBundle.getString("subject_E_text"));
        subject_FX_text.setText(languageBundle.getString("subject_FX_text"));
    }






}