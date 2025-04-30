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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.WindowDragHandler;

import java.io.IOException;
import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
public class SubjectsSubPageController implements LanguageSupport {

    @FXML public Label navi_username_text, navi_login_text;
    @FXML public ImageView navi_avatar;
    @FXML public Button comments_button;
    @FXML public FontAwesomeIcon closeApp;
    @FXML public AnchorPane dragArea, subj_details_anchor;
    @FXML public ScrollPane scroll_pane;

    private Subject subject;

    private final Map<String, Label> labelMap = new HashMap<>();

    @FXML public Label subject_code, subject_abbr, subject_Type, subject_credits, subject_studyType,
            subject_semester, subject_languages, subject_completionType, subject_studentCount;
    @FXML public Label subject_A, subject_B, subject_C, subject_D, subject_E, subject_FX;
    @FXML public Label subject_teacher, subject_evaluation, subject_assesmentMethods,
            subject_evaluationMethods, subject_plannedActivities, subject_learnoutcomes, subject_courseContents;

    @FXML public Label subject_teacher_text, subject_Type_text, subject_credits_text, subject_studyType_text,
            subject_semester_text, subject_languages_text, subject_completionType_text, subject_studentCount_text,
            subject_evaluation_text, subject_assesmentMethods_text, subject_learnoutcomes_text,
            subject_courseContents_text, subject_plannedActivities_text, subject_evaluationMethods_text,
            subject_A_text, subject_B_text, subject_C_text, subject_D_text, subject_E_text, subject_FX_text;

    private final WindowDragHandler windowDragHandler = new WindowDragHandler();

    @FXML private void handleCloseApp() {
        ((Stage) closeApp.getScene().getWindow()).close();
    }


    @FXML private void initialize() {
        LanguageManager.getInstance().registerController(this);
        LanguageManager.changeLanguage(PreferenceServise.get("LANGUAGE").toString());
        updateUILanguage(LanguageManager.getCurrentBundle());

        UserModel user = UserService.getInstance().getCurrentUser();
        if (user != null) {
            navi_username_text.setText(user.getUsername());
            navi_login_text.setText(user.getLogin());
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
        }

        windowDragHandler.setupWindowDragging(dragArea);
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        if (subject != null) {
            updateContent(subject);
            Platform.runLater(this::display_details);
        } else {
            Logger.info("Subject is null");
        }
    }

    private void setLabelText(Label label, String value, String defaultText) {
        if (label != null) {
            label.setText((value != null && !value.isBlank()) ? value.replace("\\n", "\n") : defaultText);
        } else {
            Logger.error("Label is null while trying to set text: "+ defaultText);
        }
    }

    private void updateContent(Subject s) {
        setLabelText(subject_code, s.getCode(), "Subject code not specified");
        setLabelText(subject_abbr, s.getName(), "Subject name not specified");
        setLabelText(subject_Type, s.getType(), "Subject type not specified");
        setLabelText(subject_credits, s.getCredits() != 0 ? String.valueOf(s.getCredits()) : null, "Credits not specified");
        setLabelText(subject_studyType, s.getStudyType(), "Study type not specified");
        setLabelText(subject_semester, s.getSemester(), "Semester not specified");
        setLabelText(subject_languages, s.getLanguages() != null ? s.getLanguages().toString() : null, "Languages not specified");
        setLabelText(subject_completionType, s.getCompletionType(), "Completion type not specified");
        setLabelText(subject_studentCount, s.getStudentCount() != 0 ? String.valueOf(s.getStudentCount()) : null, "Student count not specified");

        setLabelText(subject_A, s.getAScore() != null ? s.getAScore() + "%" : null, "Grade A not specified");
        setLabelText(subject_B, s.getBScore() != null ? s.getBScore() + "%" : null, "Grade B not specified");
        setLabelText(subject_C, s.getCScore() != null ? s.getCScore() + "%" : null, "Grade C not specified");
        setLabelText(subject_D, s.getDScore() != null ? s.getDScore() + "%" : null, "Grade D not specified");
        setLabelText(subject_E, s.getEScore() != null ? s.getEScore() + "%" : null, "Grade E not specified");
        setLabelText(subject_FX, s.getFxScore() != null ? s.getFxScore() + "%" : null, "Grade FX not specified");

        setLabelText(subject_teacher, s.getTeachers().toString(), "Teacher not specified");
        setLabelText(subject_evaluation, s.getEvaluation(), "Evaluation not specified");
        setLabelText(subject_assesmentMethods, s.getAssesmentMethods(), "Assessment methods not specified");
        setLabelText(subject_evaluationMethods, s.getEvaluationMethods(), "Evaluation methods not specified");
        setLabelText(subject_plannedActivities, s.getPlannedActivities(), "Planned activities not specified");
        setLabelText(subject_learnoutcomes, s.getLearningOutcomes(), "Learning outcomes not specified");
        setLabelText(subject_courseContents, s.getCourseContents(), "Course contents not specified");
    }


    @FXML public AnchorPane display_details() {
        subj_details_anchor.getChildren().clear();
        updateContent(subject);
        VBox container = new VBox(5);

        Map<String, String> info = Map.of(
                "subject_teacher_text", subject_teacher != null ? subject_teacher.getText() : "",
                "subject_evaluation_text", subject_evaluation != null ? subject_evaluation.getText() : "",
                "subject_assesmentMethods_text", subject_assesmentMethods != null ? subject_assesmentMethods.getText() : "",
                "subject_evaluationMethods_text", subject_evaluationMethods != null ? subject_evaluationMethods.getText() : "",
                "subject_plannedActivities_text", subject_plannedActivities != null ? subject_plannedActivities.getText() : "",
                "subject_learnoutcomes_text", subject_learnoutcomes != null ? subject_learnoutcomes.getText() : "",
                "subject_courseContents_text", subject_courseContents != null ? subject_courseContents.getText() : ""
        );

        ResourceBundle bundle = LanguageManager.getCurrentBundle();
        info.forEach((key, val) -> {
            if (val != null && !val.isBlank()) {
                AnchorPane block = createDetailModule(bundle.getString(key), val);
                container.getChildren().add(block);
            }
        });

        scroll_pane.setContent(container);
        scroll_pane.setFitToWidth(true);
        scroll_pane.getStyleClass().add("transparent-scroll-pane");
        subj_details_anchor.getChildren().add(scroll_pane);
        return subj_details_anchor;
    }

    private AnchorPane createDetailModule(String title, String content) {
        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: #2F3541; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-padding: 15;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        AnchorPane.setTopAnchor(titleLabel, 0.0);
        AnchorPane.setLeftAnchor(titleLabel, 0.0);

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-wrap-text: true;");
        AnchorPane.setTopAnchor(contentLabel, 20.0);
        AnchorPane.setLeftAnchor(contentLabel, 0.0);
        AnchorPane.setRightAnchor(contentLabel, 0.0);

        pane.getChildren().addAll(titleLabel, contentLabel);
        pane.setPrefWidth(400);

        return pane;
    }

    @FXML public void handleCommentsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConfig.getCOMMENTS_PAGE_PATH()));
            Parent root = loader.load();
            CommentsPageController controller = loader.getController();
            controller.setDatas(1, subject.getCode());

            Stage currentStage = (Stage) comments_button.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load Comments Page: "+ e.getMessage());
        }
    }

    public void updateUILanguage(ResourceBundle bundle) {
        Map<Label, String> localizedLabels = Map.ofEntries(
                Map.entry(subject_teacher_text, "subject_teacher_text"),
                Map.entry(subject_Type_text, "subject_Type_text"),
                Map.entry(subject_credits_text, "subject_credits_text"),
                Map.entry(subject_studyType_text, "subject_studyType_text"),
                Map.entry(subject_semester_text, "subject_semester_text"),
                Map.entry(subject_languages_text, "subject_languages_text"),
                Map.entry(subject_completionType_text, "subject_completionType_text"),
                Map.entry(subject_studentCount_text, "subject_studentCount_text"),
                Map.entry(subject_evaluation_text, "subject_evaluation_text"),
                Map.entry(subject_assesmentMethods_text, "subject_assesmentMethods_text"),
                Map.entry(subject_learnoutcomes_text, "subject_learnoutcomes_text"),
                Map.entry(subject_courseContents_text, "subject_courseContents_text"),
                Map.entry(subject_plannedActivities_text, "subject_plannedActivities_text"),
                Map.entry(subject_evaluationMethods_text, "subject_evaluationMethods_text"),
                Map.entry(subject_A_text, "subject_A_text"),
                Map.entry(subject_B_text, "subject_B_text"),
                Map.entry(subject_C_text, "subject_C_text"),
                Map.entry(subject_D_text, "subject_D_text"),
                Map.entry(subject_E_text, "subject_E_text"),
                Map.entry(subject_FX_text, "subject_FX_text")
        );

        localizedLabels.forEach((label, key) -> {
            if (label != null && bundle.containsKey(key)) {
                label.setText(bundle.getString(key));
            }
        });
    }
}
