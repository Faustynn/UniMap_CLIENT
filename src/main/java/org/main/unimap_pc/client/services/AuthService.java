package org.main.unimap_pc.client.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.TokenRefresher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuthService {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final DataFetcher dataFetcher = new DataFetcher();
    private static TokenRefresher tokenRefresher;

    public static CompletableFuture<Boolean> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = username + ":" + password;
                return sendAuthenticationRequest(AppConfig.getAUTH_URL(), data).join();
            } catch (Exception e) {
                Logger.error("Error during login for user: " + username + " - " + e.getMessage());
                return false;
            }
        });
    }

    private static CompletableFuture<Boolean> sendAuthenticationRequest(String url, String credentials) {
        if (credentials == null) return CompletableFuture.completedFuture(false);

        JSONObject requestBody = new JSONObject().put("data", credentials);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode json = mapper.readTree(response.body());
                            JsonNode user = json.get("user");
                            String accessToken = json.get("accessToken").asText();
                            String refreshToken = extractRefreshToken(response);

                            if (accessToken != null && refreshToken != null) {
                                PreferenceServise.put("ACCESS_TOKEN", accessToken);
                                PreferenceServise.put("REFRESH_TOKEN", refreshToken);

                                handleAvatar(user);

                                PreferenceServise.put("USER_DATA", user.toString());
                                tokenRefresher = new TokenRefresher(new JWTService());
                                tokenRefresher.startTokenRefreshTask();
                                dataFetcher.fetchData();
                                return true;
                            }
                            Logger.error("Tokens not found in the response.");
                            return false;
                        } catch (Exception e) {
                            Logger.error("Failed to parse JSON response: " + e.getMessage());
                            return false;
                        }
                    } else {
                        Logger.error("Authentication failed with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(e -> {
                    Logger.error("Authentication request failed: " + e.getMessage());
                    return false;
                });
    }

    private static String extractRefreshToken(HttpResponse<String> response) {
        return response.headers().firstValue("Set-Cookie")
                .flatMap(cookie -> List.of(cookie.split(";")).stream()
                        .filter(part -> part.trim().startsWith("refreshToken="))
                        .map(part -> part.substring("refreshToken=".length()))
                        .findFirst())
                .orElse(null);
    }

    private static void handleAvatar(JsonNode user) {
        JsonNode data = user.get("avatar");
        JsonNode name = user.get("avatarFileName");
        ObjectNode userNode = (ObjectNode) user;

        if (data == null || name == null || data.asText().isBlank() || name.asText().isBlank()) {
            userNode.put("avatarBinary", "");
            userNode.put("avatarName", "2.png");
            userNode.put("avatar.jpg", "2.png");
            return;
        }

        String fileName = name.asText();
        String fileData = data.asText();

        File targetDir = isDefaultAvatar(fileName)
                ? new File("src/main/resources/org/main/unimap_pc/images/avatares")
                : new File("src/main/resources/org/main/unimap_pc/images/avatares/custom");

        if (!targetDir.exists()) targetDir.mkdirs();

        File avatarFile = new File(targetDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(avatarFile)) {
            fos.write(Base64.getDecoder().decode(fileData));
            userNode.put("avatarBinary", fileData);
            userNode.put("avatarName", fileName);
            userNode.put("avatar.jpg", fileName);
        } catch (IOException e) {
            Logger.error("Failed to save avatar image: " + e.getMessage());
            userNode.put("avatarBinary", "");
            userNode.put("avatarName", "2.png");
            userNode.put("avatar.jpg", "2.png");
        }
    }

    private static boolean isDefaultAvatar(String fileName) {
        return List.of("0.png", "1.png", "2.png", "3.png", "4.png", "5.png", "6.png", "7.png", "8.png", "9.png")
                .contains(fileName);
    }

    public static CompletableFuture<Boolean> refreshAccessToken() {
        String refreshToken = String.valueOf(PreferenceServise.get("REFRESH_TOKEN"));
        if (refreshToken == null || refreshToken.isBlank()) return CompletableFuture.completedFuture(false);

        JSONObject requestBody = new JSONObject().put("refreshToken", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getREFRESH_TOKENS_URL()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            String newToken = new ObjectMapper()
                                    .readTree(response.body())
                                    .get("accessToken")
                                    .asText();
                            if (newToken != null) {
                                PreferenceServise.put("ACCESS_TOKEN", newToken);
                                return true;
                            }
                        } catch (Exception e) {
                            Logger.error("Failed to parse token refresh response: " + e.getMessage());
                        }
                    } else {
                        Logger.error("Token refresh failed with status: " + response.statusCode());
                    }
                    return false;
                })
                .exceptionally(e -> {
                    Logger.error("Refresh request error: " + e.getMessage());
                    return false;
                });
    }

    public static void handleSuccessfulAuth2Login(String response, String refreshToken) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            JsonNode user = json.get("user");
            String accessToken = json.get("accessToken").asText();

            if (accessToken != null && refreshToken != null) {
                PreferenceServise.put("ACCESS_TOKEN", accessToken);
                PreferenceServise.put("REFRESH_TOKEN", refreshToken);

                handleAvatar(user);

                PreferenceServise.put("USER_DATA", user.toString());
                tokenRefresher = new TokenRefresher(new JWTService());
                tokenRefresher.startTokenRefreshTask();
                dataFetcher.fetchData();
            } else {
                Logger.error("Tokens not found in the response.");
            }
        } catch (Exception e) {
            Logger.error("Failed to parse JSON response: " + e.getMessage());
        }
    }



}