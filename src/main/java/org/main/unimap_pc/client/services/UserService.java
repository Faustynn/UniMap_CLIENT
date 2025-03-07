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
import org.main.unimap_pc.client.utils.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.net.http.HttpClient;



@Getter
@Setter
public class UserService {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

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

    public static CompletableFuture<Boolean> delete_user(String id) {
        System.out.println("I am in delete_user with id: " + id);

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (id != null){
                    return sendDeleteUserInfoRequest(AppConfig.getDeleteUserUrl(id)).join();
                }
                return false;
            } catch (Exception e) {
                Logger.error("Error during deleting user info for user: " + id + " - " + e.getMessage());
                return false;
            }
        });
    }
    public static CompletableFuture<Boolean> delete_all_user_comments(String id) {
        System.out.println("I am in delete_all_user_comments with id: " + id);
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (id != null){
                    System.out.println("QWER id: " + id);
                    return sendDeleteUserCommentsRequest(AppConfig.getDeleteCommentsUserUrl(id)).join();
                }
                return false;
            } catch (Exception e) {
                Logger.error("Error during deleting user comments for user: " + id + " - " + e.getMessage());
                return false;
            }
        });
    }

    private static CompletableFuture<Boolean> sendDeleteUserInfoRequest(String url) {
        System.out.println("I send delete request to: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        Logger.error("Authentication failed with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Authentication request failed: " + throwable.getMessage());
                    return false;
                });
    }
    private static CompletableFuture<Boolean> sendDeleteUserCommentsRequest(String url) {
        System.out.println("I send delete comm. request to: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        Logger.error("Authentication failed with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Authentication request failed: " + throwable.getMessage());
                    return false;
                });
    }

}