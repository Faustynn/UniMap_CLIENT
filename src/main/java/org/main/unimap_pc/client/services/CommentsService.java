package org.main.unimap_pc.client.services;

import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class CommentsService {
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static CompletableFuture<String> loadAllSubjectComments(String subjectID) {
        return sendGet(AppConfig.getAllSubjectsURL(subjectID), "Load All Subject comments");
    }

    public static CompletableFuture<String> loadAllTeacherComments(String teacherID) {
        return sendGet(AppConfig.getAllTeacherURL(teacherID), "Load All Teacher comments");
    }

    public static CompletableFuture<Boolean> putNewSubjectComment(String jsonComment) {
        return sendPost(AppConfig.getADD_SUBJECTS_COMMENT_URL(), jsonComment);
    }

    public static CompletableFuture<Boolean> putNewTeacherComment(String jsonComment) {
        return sendPost(AppConfig.getADD_TEACHERS_COMMENT_URL(), jsonComment);
    }

    public static CompletableFuture<Boolean> deleteSubjectComment(String commentId) {
        return sendDelete(AppConfig.getDeleteSubjectsCommentURL(commentId));
    }

    public static CompletableFuture<Boolean> deleteTeacherComment(String commentId) {
        return sendDelete(AppConfig.getDeleteTeacherCommentURL(commentId));
    }

    private static CompletableFuture<String> sendGet(String url, String context) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        Logger.error(context + " failed with status code: " + response.statusCode());
                        return null;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error(context + " request failed: " + throwable.getMessage());
                    return null;
                });
    }

    private static CompletableFuture<Boolean> sendPost(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 201)
                .exceptionally(throwable -> {
                    Logger.error("POST request failed: " + throwable.getMessage());
                    return false;
                });
    }

    private static CompletableFuture<Boolean> sendDelete(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 204)
                .exceptionally(throwable -> {
                    Logger.error("DELETE request failed: " + throwable.getMessage());
                    return false;
                });
    }
}
