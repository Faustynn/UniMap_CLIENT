package org.main.unimap_pc.client.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.TokenRefresher;

public class AuthService {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    public static final Preferences prefs = Preferences.userNodeForPackage(AuthService.class);
    private static TokenRefresher tokenRefresher;
    private static final DataFetcher dataFetcher = new DataFetcher();

    public static CompletableFuture<Boolean> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = username + ":" + password;
                return sendAuthenticationRequest(AppConfig.getAuthUrl(), data).join();
            } catch (Exception e) {
                Logger.error("Error during login for user: " + username + " - " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    private static CompletableFuture<Boolean> sendAuthenticationRequest(String url, String encryptedData) {
        if (encryptedData == null) {
            return CompletableFuture.completedFuture(false);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("data", encryptedData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(response.body());
                            JsonNode userNode = jsonNode.get("user");
                            String accessToken = jsonNode.get("accessToken").asText();

                            String refreshToken = response.headers().firstValue("Set-Cookie")
                                    .map(cookie -> {
                                        for (String part : cookie.split(";")) {
                                            if (part.trim().startsWith("refreshToken=")) {
                                                return part.substring("refreshToken=".length());
                                            }
                                        }
                                        return null;
                                    }).orElse(null);

                            if (accessToken != null && refreshToken != null) {
                                prefs.put("ACCESS_TOKEN", accessToken);
                                prefs.put("REFRESH_TOKEN", refreshToken);
                                prefs.put("USER_DATA", userNode.toString());
                                tokenRefresher = new TokenRefresher(new JWTService());
                                tokenRefresher.startTokenRefreshTask();

                 //               System.out.println("Access Token: " + accessToken + "\nRefresh Token: " + refreshToken);
                                System.out.println("User Data: " + userNode.toString());
                                dataFetcher.fetchData();
                                return true;
                            } else {
                                Logger.error("Tokens not found in the response.");
                                return false;
                            }
                        } catch (Exception e) {
                            Logger.error("Failed to parse JSON response: " + e.getMessage());
                            return false;
                        }
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

    public static CompletableFuture<Boolean> refreshAccessToken() {
        String refreshToken = prefs.get("REFRESH_TOKEN", null);
        if (refreshToken == null) {
            return CompletableFuture.completedFuture(false);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("refreshToken", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getRefreshTokenUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        System.out.println("Refresh Token to refresh access token: " + refreshToken);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(response.body());
                            String newAccessToken = jsonNode.get("accessToken").asText();

                            if (newAccessToken != null) {
                                prefs.put("ACCESS_TOKEN", newAccessToken);
                                return true;
                            } else {
                                Logger.error("New access token not found in the response.");
                                return false;
                            }
                        } catch (Exception e) {
                            Logger.error("Failed to parse JSON response: " + e.getMessage());
                            return false;
                        }
                    } else {
                        System.out.println("Response body: " + response.body());
                        Logger.error("Token refresh failed with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Token refresh request failed: " + throwable.getMessage());
                    return false;
                });
    }
}