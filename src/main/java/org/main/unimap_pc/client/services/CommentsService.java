package org.main.unimap_pc.client.services;


import org.main.unimap_pc.client.configs.AppConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class CommentsService {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public static CompletableFuture<String> loadAllSubjectComments(String subjectID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getAllSubjectsURL(subjectID)))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        System.err.println("Load All Subjects comments failed with status code: " + response.statusCode());
                        return null;
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Request failed: " + throwable.getMessage());
                    return null;
                });
    }
    public static CompletableFuture<String> loadAllTeacherComments(String teacherID) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getAllTeacherURL(teacherID)))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        System.err.println("Load All Teacher comments failed with status code: " + response.statusCode());
                        return null;
                    }
                })
                .exceptionally(throwable -> {
                    System.err.println("Request failed: " + throwable.getMessage());
                    return null;
                });
    }


    public static CompletableFuture<Boolean> putNewSubjectComment(String jsonComment) {
        System.out.println("LALA"+jsonComment);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getAddSubjectsCommentURL()))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonComment))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 201)
                .exceptionally(throwable -> {
                    System.err.println("Request failed: " + throwable.getMessage());
                    return false;
                });
    }
    public static CompletableFuture<Boolean> putNewTeacherComment(String jsonComment) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getAddTeacherCommentURL()))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonComment))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 201)
                .exceptionally(throwable -> {
                    System.err.println("Request failed: " + throwable.getMessage());
                    return false;
                });
    }

    public static CompletableFuture<Boolean> deleteSubjectComment(String commentId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getDeleteSubjectsCommentURL(commentId)))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 204)
                .exceptionally(throwable -> {
                    System.err.println("Request failed: " + throwable.getMessage());
                    return false;
                });
    }
    public static CompletableFuture<Boolean> deleteTeacherComment(String commentId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getDeleteTeacherCommentURL(commentId)))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .DELETE()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 204)
                .exceptionally(throwable -> {
                    System.err.println("Request failed: " + throwable.getMessage());
                    return false;
                });
    }

}
