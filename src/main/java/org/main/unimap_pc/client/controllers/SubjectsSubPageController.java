package org.main.unimap_pc.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.*;
import org.main.unimap_pc.client.services.DataFetcher;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class SubjectsSubPageController implements LanguageSupport {
    @Setter
    @Getter
    private Subject subject;
    @FXML
    private Label closeApp,subject_A_text,subject_B_text,subject_C_text,subject_D_text,subject_E_text,subject_FX_text,subject_teacher_text,subject_evaluation_text,subject_assesmentMethods_text,subject_evaluationMethods_text,subject_plannedActivities_text,subject_code_text,subject_abbr_text,subject_studentCount_text,subject_Type_text,subject_credits_text,subject_studyType_text,subject_semester_text,subject_languages_text,subject_completionType_text,subject_learnoutcomes_text,subject_courseContents_text;
    @FXML
    private Label subject_A,subject_B,subject_C,subject_D,subject_E,subject_FX,subject_teacher,subject_evaluation,subject_assesmentMethods,subject_evaluationMethods,subject_plannedActivities,subject_code,subject_abbr,subject_studentCount,subject_Type,subject_credits,subject_studyType,subject_semester,subject_languages,subject_completionType,subject_learnoutcomes,subject_courseContents;
    @FXML
    private AnchorPane dragArea,plannedActivAnchor,learn_outcomesAnchor,courseContentsAnchor,evalMethodsAnchor,assesmentMethodsAnchor,TeachersAnchor,comments_Anchor;
    @FXML
    private ScrollPane comments_scroll_pane;
    @FXML
    private MFXTextField commentTextField;
    @FXML
    private MFXButton addCommentButton,add_comment_stars;
    @FXML
    private Label set_stars_text,write_new_comment_text,comments_text,avg_rait_text;
    @FXML
    private FontAwesomeIcon refresh_comments;

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
            loadComments(subject.getCode());
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

        TeachersAnchor.getChildren().clear();
        VBox container = new VBox();
        container.setStyle("-fx-spacing: 10; -fx-padding: 10;");

        for (TeacherSubjectRoles teacher : subject.getTeachers_roles()) {
            for (Teacher t : subject.getTeachers()) {
                Label teacherName = new Label(t.getName());
                teacherName.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

                Label roleLabel = new Label(teacher.getRoles().toString().replace("[", "").replace("]", "").replace("{", "").replace("}", "").replace("\"", "").replace("zodpovedný za predmet", "Garant"));
                roleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

                VBox vbox = new VBox(teacherName, roleLabel);
                vbox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10; -fx-margin-bottom: 10;");

                container.getChildren().add(vbox);
            }
        }
        TeachersAnchor.getChildren().add(container);
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


        addCommentButton.setText(languageBundle.getString("addCommentButton"));
        set_stars_text.setText(languageBundle.getString("set_stars_text"));
        write_new_comment_text.setText(languageBundle.getString("write_new_comment_text"));
        comments_text.setText(languageBundle.getString("comments_text"));
        avg_rait_text.setText(languageBundle.getString("avg_rait_text"));
    }


    @FXML
    public void handle_refresh_comments() {
        try {
            loadComments(subject.getCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadComments(String subjectCode) {
        CompletableFuture<String> commentsJsonFuture = DataFetcher.fetchComments(subjectCode, "subject");

        commentsJsonFuture.thenAccept(commentsJson -> {
            if (commentsJson != null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<CommentModel> commentsList = objectMapper.readValue(commentsJson, new TypeReference<List<CommentModel>>() {});
                    Platform.runLater(() -> {
                        displayComments(commentsList);
                    });
                } catch (Exception e) {
                    System.err.println("Failed to parse comments JSON: " + e.getMessage());
                }
            } else {
                System.err.println("Failed to load comments.");
            }
        });
    }
    private void displayComments(List<CommentModel> comment_list) {
        if(comment_list == null) {
            Label noCommentsLabel = new Label("No comments for this subject");
            comments_scroll_pane.setContent(noCommentsLabel);
            return;
        }
        VBox comments_container = new VBox(10);
        comments_container.setPadding(new Insets(10));

        // Clear any existing content in the scroll pane
        comments_scroll_pane.setContent(comments_container);

        for (CommentModel comments : comment_list) {
            AnchorPane newsItem = createCommentsItem(comments);
            comments_container.getChildren().add(newsItem);
        }
    }
    private AnchorPane createCommentsItem(CommentModel comments) {
        AnchorPane commentsItem = new AnchorPane();
        commentsItem.setPrefWidth(comments_Anchor.getPrefWidth() - 20);
        commentsItem.setStyle("-fx-background-color: #659ac9; -fx-background-radius: 25; -fx-border-color: #36436F; -fx-border-radius: 25; -fx-border-width: 2; -fx-border-style: solid;");

        ImageView avatar = new ImageView();
        avatar.setImage(new javafx.scene.image.Image(AppConfig.getAvatar(comments.getAvatar()).toString()));
        avatar.setLayoutX(20);
        avatar.setLayoutY(10);

        Label username = new Label(comments.getUsername());
        username.setLayoutX(60);
        username.setLayoutY(10);
        username.setTextFill(javafx.scene.paint.Color.WHITE);

        Label descriptionLabel = new Label(comments.getDescription());
        descriptionLabel.setLayoutX(20);
        descriptionLabel.setLayoutY(50);
        descriptionLabel.setPrefWidth(commentsItem.getPrefWidth() - 40);
        descriptionLabel.setWrapText(true);

        Label starsView = new Label(String.valueOf(comments.getRating()));
        starsView.setLayoutX(20);
        starsView.setLayoutY(100);
        starsView.setTextFill(javafx.scene.paint.Color.WHITE);

        commentsItem.getChildren().addAll(avatar, username, descriptionLabel, starsView);
        return commentsItem;
    }



}