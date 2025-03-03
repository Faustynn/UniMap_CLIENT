package org.main.unimap_pc.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.models.Teacher;
import org.main.unimap_pc.client.models.TeacherSubjectRoles;
import org.main.unimap_pc.client.services.PreferenceServise;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherSubPageController implements LanguageSupport {
    @Setter
    @Getter
    private Teacher teacher;
    @FXML
    private Label closeApp, teacher_aisID_text, teacher_fullname_text, teacher_email_text, teacher_phone_text, teacher_office_text, teacher_subjects_text;
    @FXML
    private Label teacher_aisID, teacher_fullname, teacher_email, teacher_phone, teacher_office;
    @FXML
    private AnchorPane dragArea, anchorScrollPane;

    private List<Teacher> teachers;

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

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;

        if (teacher != null) {
            updateContent(teacher);
            if (teacher.getSubjects() == null) {
                teacher.setSubjects(new ArrayList<>());
            }
            updateSubjectsList(teacher.getSubjects());
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

    public void updateContent(Teacher teacher) {
        if (teacher == null) {
            return;
        }
        teacher_aisID.setText(teacher.getId());
        teacher_fullname.setText(teacher.getName());
        teacher_email.setText(teacher.getEmail());
        teacher_phone.setText(teacher.getPhone());
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
        } else if (anchorScrollPane == null) {
            throw new IllegalStateException("anchorScrollPane is not initialized");
        }

        anchorScrollPane.getChildren().clear();

        VBox subjectsContainer = new VBox(10);
        subjectsContainer.setPrefWidth(anchorScrollPane.getPrefWidth());

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

        anchorScrollPane.getChildren().add(subjectsContainer);
        AnchorPane.setTopAnchor(subjectsContainer, 10.0);
        AnchorPane.setLeftAnchor(subjectsContainer, 10.0);
        AnchorPane.setRightAnchor(subjectsContainer, 10.0);
    }

    private AnchorPane createSubjectCard(TeacherSubjectRoles teacherSubjectRoles, int index) {
        AnchorPane card = new AnchorPane();
        card.setPrefHeight(100);
        card.setPrefWidth(anchorScrollPane.getPrefWidth() - 20);
        card.setStyle("-fx-background-color: " + (index % 2 == 0 ? "#f5f5f5" : "#ffffff") +
                "; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 5;");

        // Subject name
        Label nameLabel = new Label(teacherSubjectRoles.getSubjectName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(card.getPrefWidth() - 20);
        card.getChildren().add(nameLabel);
        AnchorPane.setTopAnchor(nameLabel, 10.0);
        AnchorPane.setLeftAnchor(nameLabel, 10.0);
        AnchorPane.setRightAnchor(nameLabel, 10.0);

        // Roles
        ResourceBundle bundle = LanguageManager.getCurrentBundle();
        String rolesText = bundle.containsKey("roles") ?
                bundle.getString("roles") :
                "Roles";

        String rolesStr = rolesText + ": " + teacherSubjectRoles.getFormattedRoles().replace("{", "").replace("}", "").replace("\"", "").replace("null", "Not specified").replace("zodpovedný za predmet", "Garant");

        Label rolesLabel = new Label(rolesStr);
        rolesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #424242;");
        rolesLabel.setWrapText(true);
        rolesLabel.setMaxWidth(card.getPrefWidth() - 20);
        card.getChildren().add(rolesLabel);
        AnchorPane.setTopAnchor(rolesLabel, 40.0);
        AnchorPane.setLeftAnchor(rolesLabel, 10.0);
        AnchorPane.setRightAnchor(rolesLabel, 10.0);

        return card;
    }
}