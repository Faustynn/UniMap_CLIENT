package org.main.unimap_pc.client.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;
import org.main.unimap_pc.client.utils.TokenRefresher;

public class AuthService {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private static TokenRefresher tokenRefresher;
    private static final DataFetcher dataFetcher = new DataFetcher();

    public static CompletableFuture<Boolean> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = username + ":" + password;
                return sendAuthenticationRequest(AppConfig.getAuthUrl(), data).join();
            } catch (Exception e) {
                Logger.error("Error during login for user: " + username + " - " + e.getMessage());
                return false;
            }
        });
    }

    private static CompletableFuture<Boolean> sendAuthenticationRequest(String url, String encryptedData) {
        if (encryptedData == null) {
            return CompletableFuture.completedFuture(false);
        }
        System.out.println("I send auth request with data: " + encryptedData);

        JSONObject requestBody = new JSONObject();
        requestBody.put("data", encryptedData);
        System.out.println("Request body: " + requestBody.toString());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                       //     System.out.println("Auth response body: " + response.body());

                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(response.body());
                            JsonNode userNode = jsonNode.get("user");
                            String accessToken = jsonNode.get("accessToken").asText();
                           // System.out.println("!User node: " + userNode.toString());

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
                                PreferenceServise.put("ACCESS_TOKEN", accessToken);
                                PreferenceServise.put("REFRESH_TOKEN", refreshToken);


                                JsonNode avatarData = userNode.get("avatar");
                                JsonNode avatarName = userNode.get("avatarFileName");

                                if (avatarData != null && !avatarData.asText().isEmpty() && !avatarData.asText().equals("null") &&
                                        avatarName != null && !avatarName.asText().isEmpty() && !avatarName.asText().equals("null")) {

                                    // Parse the avatar.jpg name and binary
                                    String fileName = avatarName.asText();
                                    String fileData = avatarData.asText();

                                    // Create directory if it doesn't exist
                                    String customAvatarFolderPath = "src/main/resources/org/main/unimap_pc/images/avatares/custom";
                                    String defltAvatarFolderPath = "src/main/resources/org/main/unimap_pc/images/avatares";
                                    File customAvatarFolder = new File(customAvatarFolderPath);
                                    File defltAvatarFolder = new File(defltAvatarFolderPath);
                                    if (!customAvatarFolder.exists()) {
                                        customAvatarFolder.mkdirs();
                                    }

                                    File avatarFile;
                                    if (avatarName.asText().equals("0.png") ||
                                            avatarName.asText().equals("1.png") ||
                                            avatarName.asText().equals("2.png") ||
                                            avatarName.asText().equals("3.png") ||
                                            avatarName.asText().equals("4.png") ||
                                            avatarName.asText().equals("5.png") ||
                                            avatarName.asText().equals("6.png") ||
                                            avatarName.asText().equals("7.png") ||
                                            avatarName.asText().equals("8.png") ||
                                            avatarName.asText().equals("9.png")) {
                                        avatarFile = new File(defltAvatarFolder, avatarName.asText());
                                    }else{
                                        avatarFile = new File(customAvatarFolder, avatarName.asText());
                                    }


                                    try {
                                        byte[] imageBytes = Base64.getDecoder().decode(fileData);
                                        try (FileOutputStream fos = new FileOutputStream(avatarFile)) {
                                            fos.write(imageBytes);
                                            System.out.println("Avatar saved to: " + avatarFile.getAbsolutePath());
                                        }

                                        // Store the filename in the user node
                                        ((ObjectNode) userNode).put("avatarBinary", fileData);
                                        ((ObjectNode) userNode).put("avatarName", fileName);
                                        ((ObjectNode) userNode).put("avatar.jpg", fileName);

                                    } catch (IOException e) {
                                        Logger.error("Failed to save avatar.jpg image: " + e.getMessage());
                                        ((ObjectNode) userNode).put("avatarBinary", "");
                                        ((ObjectNode) userNode).put("avatarName", "2.png");
                                        ((ObjectNode) userNode).put("avatar.jpg", "2.png");
                                    }
                                } else {
                                    System.out.println("Avatar is null or empty, setting default value.");
                                    ((ObjectNode) userNode).put("avatarBinary", "");
                                    ((ObjectNode) userNode).put("avatarName", "2.png");
                                    ((ObjectNode) userNode).put("avatar.jpg", "2.png");
                                }

                                PreferenceServise.put("USER_DATA", userNode.toString());

                                tokenRefresher = new TokenRefresher(new JWTService());
                                tokenRefresher.startTokenRefreshTask();

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
                        System.out.println("Auth FAILED Response body: " + response.statusCode() + response.body());
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
        String refreshToken = PreferenceServise.get("REFRESH_TOKEN").toString();
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
                                PreferenceServise.put("ACCESS_TOKEN", newAccessToken);
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