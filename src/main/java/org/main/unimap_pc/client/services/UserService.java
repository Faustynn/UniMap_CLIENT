package org.main.unimap_pc.client.services;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.models.UserModel;

import java.io.IOException;
import java.util.Objects;

@Getter
@Setter
public class UserService {
    private static UserService instance;
    private UserModel currentUser;
    private UserService() {}

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void autoLogin(Stage curr_stage) throws IOException {
        System.out.println("Auto login with refresh token");

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getMainPagePath())));
        Scene mainScene = new Scene(root);
        Platform.runLater(() -> {
            curr_stage.setScene(mainScene);
            curr_stage.show();
        });
    }
}