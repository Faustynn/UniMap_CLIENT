package org.main.unimap_pc.client.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;

public class DataFetcher {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public CompletableFuture<Void> fetchData() {
        return fetchSubjects()
                .thenCompose(subjectsFetched -> fetchTeachers())
                .thenCompose(newsfetch -> fetchNews())
                .thenAccept(teachersFetched -> {
                    System.out.println("Data fetching completed.");
                });
    }




    private CompletableFuture<Boolean> fetchSubjects() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getSUBJECTS_URL()))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .GET()
                .build();
        System.out.println("Subjects token sended "+PreferenceServise.get("ACCESS_TOKEN"));

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(response.body());

                            System.out.println(jsonNode);
                            CacheService.put("SUBJECTS", jsonNode.toString());
                            return true;
                        } catch (Exception e) {
                            Logger.error("Failed to parse JSON response: " + e.getMessage());
                            return false;
                        }
                    } else {
                        Logger.error("Failed to fetch subjects with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Subjects fetch request failed: " + throwable.getMessage());
                    return false;
                });
    }

    public static CompletableFuture<String> fetchNews() {
        String accessToken = (String) PreferenceServise.get("ACCESS_TOKEN");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getGET_NEWS_URL()))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        Logger.error("Failed to fetch news. HTTP status code: " + response.statusCode());
                        return null;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("News fetch request failed: " + throwable.getMessage());
                    return null;
                });
    }


    private CompletableFuture<Boolean> fetchTeachers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AppConfig.getTEACHERS_URL()))
                .header("Authorization", "Bearer " + PreferenceServise.get("ACCESS_TOKEN"))
                .GET()
                .build();
        System.out.println("Teacher token sended "+PreferenceServise.get("ACCESS_TOKEN"));

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode jsonNode = objectMapper.readTree(response.body());

                            System.out.println(jsonNode);
                            CacheService.put("TEACHERS", jsonNode.toString());
                            return true;
                        } catch (Exception e) {
                            Logger.error("Failed to parse JSON response: " + e.getMessage());
                            return false;
                        }
                    } else {
                        Logger.error("Failed to fetch teachers with status code: " + response.statusCode());
                        return false;
                    }
                })
                .exceptionally(throwable -> {
                    Logger.error("Teachers fetch request failed: " + throwable.getMessage());
                    return false;
                });
    }

}