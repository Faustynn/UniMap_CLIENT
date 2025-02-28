package org.main.unimap_pc.client.controllers;

import javafx.fxml.FXML;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.models.Subject;
import org.main.unimap_pc.client.services.DataFetcher;

public class SubjectsSubPageController {
    @Setter
    @Getter
    private Subject subject;

    public void loadSubjectData() {
        if (subject != null) {
            System.out.println("Subject: " + subject);
            DataFetcher.fetchComments(subject.getCode());
        }
    }

    @FXML
    private void initialize() {

    }
}