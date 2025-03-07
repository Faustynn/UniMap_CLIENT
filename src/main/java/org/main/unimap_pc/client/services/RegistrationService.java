package org.main.unimap_pc.client.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;

public class RegistrationService {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public static CompletableFuture<Boolean> registration(String username, String email, String login, String password, AtomicInteger code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = username + ":" + email + ":" + login + ":" + password;
                return sendRegistrationRequest(AppConfig.getRegistrUrl(), data,code).join();
            } catch (Exception e) {
                Logger.error("Error during registration for user: " + username + " - " + e.getMessage());
                return false;
            }
        });
    }

    private static CompletableFuture<Boolean> sendRegistrationRequest(String url, String encryptedData, AtomicInteger code) {
        if (encryptedData == null) {
            return CompletableFuture.completedFuture(false);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("data", encryptedData);
        System.out.println(encryptedData);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        System.out.println("Full Request: " + request.toString());
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return true;
                    } else if (response.statusCode() == 303) {
                        code.set(303);
                        return false;
                    } else if (response.statusCode() == 304) {
                        code.set(304);
                        return false;
                    }else if (response.statusCode() == 305) {
                        code.set(305);
                        return false;
                    } else {
                        Logger.error("Registration failed with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Registration request failed: " + throwable.getMessage());
                    return false;
                });
    }


}