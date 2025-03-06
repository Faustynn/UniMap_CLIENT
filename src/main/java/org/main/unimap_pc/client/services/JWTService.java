package org.main.unimap_pc.client.services;

import lombok.Setter;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.TokenRefresher;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.main.unimap_pc.client.configs.AppConfig;

import java.io.IOException;
import java.util.Objects;

@Setter
public class JWTService {

    private TokenRefresher tokenRefresher;

    public void refreshAccessToken() {
            AuthService.refreshAccessToken().thenAccept(isTokenRefreshed -> {
                if (isTokenRefreshed) {
                    if (tokenRefresher != null) {
                        tokenRefresher.startTokenRefreshTask();
                    }
                } else {
                    handleInvalidToken();
                }
            });
    }

    private void handleInvalidToken() {
        if (tokenRefresher != null) {
            tokenRefresher.stopTokenRefreshTask();
        }
        PreferenceServise.remove("ACCESS_TOKEN");
        PreferenceServise.remove("REFRESH_TOKEN");
        PreferenceServise.remove("USER_DATA");

        redirectToLoginPage();
    }

    private void redirectToLoginPage() {
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(AppConfig.getLoginPagePath())));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Logger.error("Error redirecting to login page: " + e.getMessage());
            e.printStackTrace();
        }
    }
}