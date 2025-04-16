package org.main.unimap_pc.client.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
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
import org.main.unimap_pc.client.models.Teacher;
import org.main.unimap_pc.client.models.TeacherSubjectRoles;
import org.main.unimap_pc.client.models.UserModel;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;
import org.main.unimap_pc.client.utils.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

@Getter
@Setter
@Accessors(chain = true)
public class TeacherSubPageController implements LanguageSupport {
    @FXML
    public Label navi_username_text,navi_login_text;
    @FXML
    public ImageView navi_avatar;

    public Button comments_button;
    private Teacher teacher_entity;

    @FXML
    private FontAwesomeIcon closeApp;

    @FXML
    private Label teacher_aisID_text, teacher_fullname_text, teacher_email_text,
            teacher_phone_text, teacher_office_text, teacher_subjects_text;

    @FXML
    private Label teacher_aisID, teacher_fullname, teacher_email,
            teacher_phone, teacher_office;

    @FXML
    private AnchorPane dragArea, teachers_details_anchor;

    @FXML
    private ScrollPane scroll_pane;

    private List<Teacher> teachers;

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    private void handleCloseApp() {
        Stage stage = (Stage) closeApp.getScene().getWindow();
        stage.close();
    }

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

    public void setTeacher_entity(Teacher teacher_entity) {
        this.teacher_entity = teacher_entity;

        if (teacher_entity != null) {
            updateContent(teacher_entity);
            if (teacher_entity.getSubjects() == null) {
                teacher_entity.setSubjects(new ArrayList<>());
            }
            updateSubjectsList(teacher_entity.getSubjects());
        }
    }

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
            navi_avatar.setImage(AppConfig.getAvatar(user.getAvatarName()));
        }

        dragArea.setOnMousePressed(this::handleMousePressed);
        dragArea.setOnMouseDragged(this::handleMouseDragged);

        // Configure scroll pane
        scroll_pane.setFitToWidth(true);
        scroll_pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll_pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void updateContent(Teacher teacher) {
        if (teacher == null) {
            return;
        }
        teacher_aisID.setText(teacher.getId() != null && !teacher.getId().isEmpty() ? teacher.getId() : "Not specified");
        teacher_fullname.setText(teacher.getName() != null && !teacher.getName().isEmpty() ? teacher.getName() : "Not specified");
        teacher_email.setText(teacher.getEmail() != null && !teacher.getEmail().isEmpty() ? teacher.getEmail() : "Not specified");
        teacher_phone.setText(teacher.getPhone() != null && !teacher.getPhone().isEmpty() ? teacher.getPhone() : "Not specified");
        teacher_office.setText(teacher.getOffice() != null && !teacher.getOffice().isEmpty() ? teacher.getOffice() : "Not specified");
    }

    public void updateUILanguage(ResourceBundle languageBundle) {
        teacher_aisID_text.setText(languageBundle.getString("teacher_aisID"));
        teacher_fullname_text.setText(languageBundle.getString("teacher_fullname"));
        teacher_email_text.setText(languageBundle.getString("teacher_email"));
        teacher_phone_text.setText(languageBundle.getString("teacher_phone"));
        teacher_office_text.setText(languageBundle.getString("teacher_office"));
        teacher_subjects_text.setText(languageBundle.getString("teacher_subjects"));
    }

    private void updateSubjectsList(List<TeacherSubjectRoles> subjects) {
        if (subjects == null) {
            throw new IllegalStateException("Subjects list is not initialized");
        }

        VBox subjectsContainer = new VBox(5);
        subjectsContainer.setStyle("-fx-padding: 10px;");

        ResourceBundle bundle = LanguageManager.getCurrentBundle();
        String noSubjectsText = bundle.containsKey("no_subjects") ?
                bundle.getString("no_subjects") :
                "This teacher has no subjects";

        if (subjects.isEmpty()) {
            Label noResultsLabel = new Label(noSubjectsText);
            noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");
            subjectsContainer.getChildren().add(noResultsLabel);
        } else {
            for (int i = 0; i < subjects.size(); i++) {
                TeacherSubjectRoles teacherSubjectRoles = subjects.get(i);
                AnchorPane subjectCard = createSubjectCard(teacherSubjectRoles, i);
                subjectsContainer.getChildren().add(subjectCard);
            }
        }

        scroll_pane.setContent(subjectsContainer);
        scroll_pane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-control-inner-background: transparent;"

        );
    }

    private AnchorPane createSubjectCard(TeacherSubjectRoles teacherSubjectRoles, int index) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(70);
        card.setMinWidth(380);
        card.setStyle(
                "-fx-background-color: #2F3541;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 15;"
        );

        // Subject name
        Label nameLabel = new Label(teacherSubjectRoles.getSubjectName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 0.0);
        AnchorPane.setLeftAnchor(nameLabel, 0.0);

        // Roles
        String rolesStr = teacherSubjectRoles.getFormattedRoles()
                .replace("{", "")
                .replace("}", "")
                .replace("\"", "")
                .replace("null", "Not specified")
                .replace("zodpovedný za predmet", "Garant");

        Label rolesLabel = new Label(rolesStr);
        rolesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        rolesLabel.setWrapText(true);
        rolesLabel.setMaxWidth(350);
        card.getChildren().add(rolesLabel);
        AnchorPane.setTopAnchor(rolesLabel, 25.0);
        AnchorPane.setLeftAnchor(rolesLabel, 0.0);

        return card;
    }

    @FXML
    public void handleCommentsButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(AppConfig.getCommentsPagePath()));
            Parent root = loader.load();

            CommentsPageController controller = loader.getController();
            controller.setDatas(2, teacher_entity.getId());


            Stage currentStage = (Stage) comments_button.getScene().getWindow();
            Scene mainScene = new Scene(root);
            currentStage.setScene(mainScene);
            currentStage.show();
        } catch (IOException e) {
            Logger.error("Failed to load Comments Page from TeacherSub page: " + e.getMessage());
        }
    }
}