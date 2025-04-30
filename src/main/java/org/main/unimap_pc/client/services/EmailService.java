package org.main.unimap_pc.client.services;

import org.json.JSONObject;
import org.main.unimap_pc.client.utils.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class EmailService {
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static CompletableFuture<Boolean> checkEmail(String url, String email) {
        if (isBlank(email)) return CompletableFuture.completedFuture(false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        return sendSimpleRequest(request, "Check Email");
    }

    public static CompletableFuture<Boolean> checkCode(String url, String code, String email) {
        if (isBlank(email) || isBlank(code)) return CompletableFuture.completedFuture(false);

        JSONObject requestBody = new JSONObject().put("data", email + ":" + code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return sendSimpleRequest(request, "Check Code");
    }

    public static CompletableFuture<Boolean> updatePassword(String url, String newPassword, String email) {
        if (isBlank(email) || isBlank(newPassword)) return CompletableFuture.completedFuture(false);

        JSONObject requestBody = new JSONObject().put("data", email + ":" + newPassword);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return sendSimpleRequest(request, "Update Password");
    }

    private static CompletableFuture<Boolean> sendSimpleRequest(HttpRequest request, String context) {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    boolean success = response.statusCode() == 200;
                    if (!success) Logger.error(context + " failed with status code: " + response.statusCode());
                    return success;
                })
                .exceptionally(throwable -> {
                    Logger.error(context + " request failed: " + throwable.getMessage());
                    return false;
                });
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
