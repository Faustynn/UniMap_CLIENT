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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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
import java.util.function.BiFunction;

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
     public Label subject_teacher, subject_evaluation, subject_assesmentMethods,
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
        dragArea.setStyle("-fx-background-color: #191C22FF;");

        scroll_pane.widthProperty().addListener((obs, oldVal, newVal) -> {
            display_details();
        });

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
            System.out.println("Labelll: " + label.getText());
        } else {
     //       System.out.println("Label is null while trying to set text: " + defaultText);
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

    //    System.out.println("lang:" + s.getLanguages());
        setLabelText(subject_languages, getLanguageAbbreviations(s.getLanguages()), "Languages not specified");
        setLabelText(subject_completionType, s.getCompletionType(), "Completion type not specified");
        setLabelText(subject_studentCount, s.getStudentCount() != 0 ? String.valueOf(s.getStudentCount()) : null, "Student count not specified");

        setLabelText(subject_A, s.getAScore() != null ? s.getAScore() + "%" : null, "Grade A not specified");
        setLabelText(subject_B, s.getBScore() != null ? s.getBScore() + "%" : null, "Grade B not specified");
        setLabelText(subject_C, s.getCScore() != null ? s.getCScore() + "%" : null, "Grade C not specified");
        setLabelText(subject_D, s.getDScore() != null ? s.getDScore() + "%" : null, "Grade D not specified");
        setLabelText(subject_E, s.getEScore() != null ? s.getEScore() + "%" : null, "Grade E not specified");
        setLabelText(subject_FX, s.getFxScore() != null ? s.getFxScore() + "%" : null, "Grade FX not specified");


        setLabelText(subject_evaluation, s.getEvaluation() != null ? s.getEvaluation() : null, "Evaluation not specified");
        setLabelText(subject_assesmentMethods, s.getAssesmentMethods() != null ? s.getAssesmentMethods() : null, "Assessment methods not specified");
        setLabelText(subject_evaluationMethods, s.getEvaluationMethods() != null ? s.getEvaluationMethods() : null, "Evaluation methods not specified");
        setLabelText(subject_plannedActivities, s.getPlannedActivities() != null ? s.getPlannedActivities() : null, "Planned activities not specified");
        setLabelText(subject_learnoutcomes, s.getLearningOutcomes() != null ? s.getLearningOutcomes() : null, "Learning outcomes not specified");
        setLabelText(subject_courseContents, s.getCourseContents() != null ? s.getCourseContents() : null, "Course contents not specified");
        setLabelText(subject_teacher, s.getTeachers() != null ? s.getTeachers().toString() : null, "Teacher not specified");
    }


    private AnchorPane createDetailModule(String title, String content) {
        AnchorPane pane = new AnchorPane();
        pane.setStyle("-fx-background-color: #222834; -fx-background-radius: 10; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        AnchorPane.setTopAnchor(titleLabel, 10.0);
        AnchorPane.setLeftAnchor(titleLabel, 20.0);

        double wrappingWidth = scroll_pane.getWidth() - 80;

        Text contentText = new Text(content);
        contentText.setStyle("-fx-fill: white;");
        contentText.setFont(Font.font(13));
        contentText.setWrappingWidth(wrappingWidth);

        TextFlow textFlow = new TextFlow(contentText);
        textFlow.setLineSpacing(6.0);
        textFlow.setMaxWidth(Double.MAX_VALUE);
        textFlow.setPrefWidth(wrappingWidth);
        AnchorPane.setTopAnchor(textFlow, 40.0);
        AnchorPane.setLeftAnchor(textFlow, 20.0);
        AnchorPane.setRightAnchor(textFlow, 20.0);

        pane.getChildren().addAll(titleLabel, textFlow);

        Platform.runLater(() -> {
            contentText.applyCss();

            double avgCharWidth = contentText.getFont().getSize() * 0.8;

            int charsPerLine = (int)(wrappingWidth / avgCharWidth);
            int lines = (int) Math.ceil((double) content.length() / charsPerLine);

            double lineHeight = contentText.getFont().getSize() + textFlow.getLineSpacing();
            double totalTextHeight = lines * lineHeight;

            double titleHeight = 40;
            double padding = 40;

            double totalHeight = titleHeight + totalTextHeight + padding;

            pane.setMinHeight(totalHeight);
            pane.setPrefHeight(totalHeight);
        });
        return pane;
    }


    @FXML
    public AnchorPane display_details() {
        subj_details_anchor.getChildren().clear();
        updateContent(subject);

        VBox container = new VBox(20);
        container.setStyle("-fx-background-color: #191C22FF; -fx-padding: 20;");
        container.setFillWidth(true);

        ResourceBundle bundle = LanguageManager.getCurrentBundle();

        BiFunction<String, Label, AnchorPane> createSection = (titleKey, contentLabel) -> {
            String title = bundle.getString(titleKey);
            String content = (contentLabel != null &&
                    !contentLabel.getText().isBlank() &&
                    !contentLabel.getText().contains("not specified")) ?
                    contentLabel.getText() :
                    "Subject doesn't have " + title.toLowerCase() + " information";

            AnchorPane sectionBlock = createDetailModule(title, content);
            sectionBlock.setStyle("-fx-border-color: #ffffff; -fx-border-width: 2; -fx-border-radius: 10;");
            return sectionBlock;
        };

        parse_teachers(subject_teacher);

        container.getChildren().addAll(
                createSection.apply("subject_teacher", subject_teacher),
                createSection.apply("subject_evaluation_text", subject_evaluation),
                createSection.apply("subject_assesmentMethods_text", subject_assesmentMethods),
                createSection.apply("subject_learnoutcomes_text", subject_learnoutcomes),
                createSection.apply("subject_courseContents_text", subject_courseContents),
                createSection.apply("subject_plannedActivities_text", subject_plannedActivities),
                createSection.apply("subject_evaluationMethods_text", subject_evaluationMethods)
        );

        scroll_pane.setContent(container);
        scroll_pane.setFitToWidth(true);
        scroll_pane.setPannable(true);
        scroll_pane.getStyleClass().add("transparent-scroll-pane");
        scroll_pane.setStyle("-fx-background-color: transparent; -fx-padding: 20;");

        AnchorPane.setTopAnchor(scroll_pane, 0.0);
        AnchorPane.setBottomAnchor(scroll_pane, 0.0);
        AnchorPane.setLeftAnchor(scroll_pane, 0.0);
        AnchorPane.setRightAnchor(scroll_pane, 0.0);

        subj_details_anchor.getChildren().add(scroll_pane);
        return subj_details_anchor;
    }


   private void parse_teachers(Label label) {
        String newText = label.getText().replace("[", "").replace("]", "").replace("'", "").replace("\"", "").replace("{", "").replace("}", "\n").replace(",", "");
        label.setText(newText);
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

    private String getLanguageAbbreviations(Collection<String> languages) {
        if (languages == null || languages.isEmpty()) return null;

        List<String> cleanedLanguages = languages.stream()
                .map(lang -> lang.replaceAll("[{}\\[\\]'\"]", ""))
                .toList();

        Map<String, String> langMap = Map.of(
                "anglický jazyk", "EN",
                "slovenský jazyk", "SK",
                "nemecký jazyk", "DE",
                "ruský jazyk", "RU",
                "český jazyk", "CZ",
                "francúzsky jazyk", "FR",
                "španielsky jazyk", "ES",
                "ukrajinský jazyk", "UA"
        );

        return cleanedLanguages.stream()
                .map(lang -> langMap.getOrDefault(lang.toLowerCase(), lang))
                .distinct()
                .sorted()
                .reduce((a, b) -> a + ", " + b)
                .orElse(null);
    }

    public void updateUILanguage(ResourceBundle bundle) {
        Map<Label, String> localizedLabels = new HashMap<>();

        if (subject_teacher_text != null) localizedLabels.put(subject_teacher_text, "subject_teacher_text");
        if (subject_Type_text != null) localizedLabels.put(subject_Type_text, "subject_Type_text");
        if (subject_credits_text != null) localizedLabels.put(subject_credits_text, "subject_credits_text");
        if (subject_studyType_text != null) localizedLabels.put(subject_studyType_text, "subject_studyType_text");
        if (subject_semester_text != null) localizedLabels.put(subject_semester_text, "subject_semester_text");
        if (subject_languages_text != null) localizedLabels.put(subject_languages_text, "subject_languages_text");
        if (subject_completionType_text != null) localizedLabels.put(subject_completionType_text, "subject_completionType_text");
        if (subject_studentCount_text != null) localizedLabels.put(subject_studentCount_text, "subject_studentCount_text");

        if (subject_evaluation_text != null) localizedLabels.put(subject_evaluation_text, "subject_evaluation_text");
        if (subject_assesmentMethods_text != null) localizedLabels.put(subject_assesmentMethods_text, "subject_assesmentMethods_text");
        if (subject_learnoutcomes_text != null) localizedLabels.put(subject_learnoutcomes_text, "subject_learnoutcomes_text");
        if (subject_courseContents_text != null) localizedLabels.put(subject_courseContents_text, "subject_courseContents_text");
        if (subject_plannedActivities_text != null) localizedLabels.put(subject_plannedActivities_text, "subject_plannedActivities_text");
        if (subject_evaluationMethods_text != null) localizedLabels.put(subject_evaluationMethods_text, "subject_evaluationMethods_text");

        if (subject_A_text != null) localizedLabels.put(subject_A_text, "subject_A_text");
        if (subject_B_text != null) localizedLabels.put(subject_B_text, "subject_B_text");
        if (subject_C_text != null) localizedLabels.put(subject_C_text, "subject_C_text");
        if (subject_D_text != null) localizedLabels.put(subject_D_text, "subject_D_text");
        if (subject_E_text != null) localizedLabels.put(subject_E_text, "subject_E_text");
        if (subject_FX_text != null) localizedLabels.put(subject_FX_text, "subject_FX_text");

        localizedLabels.forEach((label, key) -> {
            if (bundle.containsKey(key)) {
                label.setText(bundle.getString(key));
            }
        });
    }


}