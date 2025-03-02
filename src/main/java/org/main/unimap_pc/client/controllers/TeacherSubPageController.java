package org.main.unimap_pc.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.models.Teacher;
import org.main.unimap_pc.client.services.UserService;
import org.main.unimap_pc.client.utils.LanguageManager;
import org.main.unimap_pc.client.utils.LanguageSupport;

import java.util.ResourceBundle;

public class TeacherSubPageController implements LanguageSupport {
    @Setter
    @Getter
    private Teacher teacher;
    @FXML
    private Label closeApp,teacher_aisID_text,teacher_fullname_text,teacher_email_text,teacher_phone_text,teacher_office_text,teacher_subjects_text;
    @FXML
    private Label teacher_aisID,teacher_fullname,teacher_email,teacher_phone,teacher_office;
    @FXML
    private AnchorPane dragArea,subjectsAnchor;

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
        }
    }

    @FXML
    private void initialize() {
        LanguageManager.getInstance().registerController(this);
        LanguageManager.changeLanguage(UserService.getInstance().getDefLang());
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
        teacher_office.setText(teacher.getOffice());
    }

    public void updateUILanguage(ResourceBundle languageBundle) {
        teacher_aisID_text.setText(languageBundle.getString("teacher_aisID"));
        teacher_fullname_text.setText(languageBundle.getString("teacher_fullname"));
        teacher_email_text.setText(languageBundle.getString("teacher_email"));
        teacher_phone_text.setText(languageBundle.getString("teacher_phone"));
        teacher_office_text.setText(languageBundle.getString("teacher_office"));
        teacher_subjects_text.setText(languageBundle.getString("teacher_subjects"));
    }

}
