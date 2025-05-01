package org.main.unimap_pc.client.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import lombok.Getter;
import okhttp3.*;
import okhttp3.sse.*;

import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.controllers.HomePageController;
import org.main.unimap_pc.client.models.NewsModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SseManager {
    private static final int RECONNECT_DELAY_SECONDS = 5;
    private static final int MAX_RECONNECT_DELAY_SECONDS = 60;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private EventSource eventSource;
    private ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
    private String lastEventId;
    private int reconnectAttempt = 0;
    public final AtomicBoolean isConnecting = new AtomicBoolean(false);

    @Getter
    private final List<NewsModel> newsList = new CopyOnWriteArrayList<>();
    private final List<Consumer<List<NewsModel>>> newsListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<ConnectionStatus>> connectionListeners = new CopyOnWriteArrayList<>();

    private HomePageController homePageController;

    public enum ConnectionStatus {
        CONNECTED, DISCONNECTED, CONNECTING, ERROR
    }

    public void addNewsListener(Consumer<List<NewsModel>> listener) {
        newsListeners.add(listener);
        if (!newsList.isEmpty()) {
            listener.accept(new ArrayList<>(newsList));
        }
    }

    public void removeNewsListener(Consumer<List<NewsModel>> listener) {
        newsListeners.remove(listener);
    }

    public void addConnectionListener(Consumer<ConnectionStatus> listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(Consumer<ConnectionStatus> listener) {
        connectionListeners.remove(listener);
    }

    public void registerHomePageController(HomePageController controller) {
        this.homePageController = controller;
    }

    public void connectToSSEServer() {
        if (!isConnecting.compareAndSet(false, true)) {
            Logger.info("Connection attempt already in progress");
            return;
        }

        try {
            notifyConnectionStatus(ConnectionStatus.CONNECTING);

            Request.Builder requestBuilder = new Request.Builder().url(AppConfig.getSSE_SUBSCRIBE_URL());
            if (lastEventId != null) {
                requestBuilder.addHeader("Last-Event-ID", lastEventId);
                Logger.info("Reconnecting with Last-Event-ID: " + lastEventId);
            }

            if (eventSource != null) {
                eventSource.cancel();
            }

            eventSource = EventSources.createFactory(client).newEventSource(requestBuilder.build(), new SseEventListener());
            Logger.info("Connecting to SSE server...");
        } finally {
            isConnecting.set(false);
        }
    }

    public void closeConnection() {
        if (eventSource != null) {
            eventSource.cancel();
            eventSource = null;
        }

        if (reconnectExecutor != null && !reconnectExecutor.isShutdown()) {
            reconnectExecutor.shutdownNow();
        }

        newsList.clear();
        notifyConnectionStatus(ConnectionStatus.DISCONNECTED);
        Logger.info("SSE connection closed.");
    }

    private void reconnectToSSEServer() {
        if (eventSource != null) {
            eventSource.cancel();
            eventSource = null;
        }

        if (reconnectExecutor == null || reconnectExecutor.isShutdown()) {
            reconnectExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        int delay = Math.min(RECONNECT_DELAY_SECONDS * (1 << Math.min(reconnectAttempt, 4)), MAX_RECONNECT_DELAY_SECONDS);
        reconnectAttempt++;
        Logger.info(String.format("Reconnecting to SSE server in %d seconds (attempt %d)", delay, reconnectAttempt));

        reconnectExecutor.schedule(this::connectToSSEServer, delay, TimeUnit.SECONDS);
    }

    private void processNewsListEvent(String data, String id) {
        try {
            if (id != null && !id.isEmpty()) {
                lastEventId = id;
            }

            List<NewsModel> updatedNews = objectMapper.readValue(data, new TypeReference<List<NewsModel>>() {});
            Platform.runLater(() -> {
                newsList.clear();
                newsList.addAll(updatedNews);
                newsListeners.forEach(listener -> {
                    try {
                        listener.accept(new ArrayList<>(newsList));
                    } catch (Exception e) {
                        Logger.error("Error notifying news listener: " + e.getMessage());
                    }
                });

                if (homePageController != null) {
                    homePageController.updateNews(updatedNews);
                }

                Logger.info("News list updated, total news: " + newsList.size());
            });
        } catch (IOException e) {
            Logger.error("Error parsing news list: " + e.getMessage());
        }
    }

    private void notifyConnectionStatus(ConnectionStatus status) {
        Platform.runLater(() -> connectionListeners.forEach(listener -> {
            try {
                listener.accept(status);
            } catch (Exception e) {
                Logger.error("Error notifying connection listener: " + e.getMessage());
            }
        }));
    }

    private void showNotification(String message, boolean isError) {
        if (isError) {
            System.err.println("ERROR: " + message);
        } else {
            System.out.println("INFO: " + message);
        }
    }

    public void shutdown() {
        Logger.info("Shutting down SSE Manager");
        closeConnection();
        newsListeners.clear();
        connectionListeners.clear();
    }

    private class SseEventListener extends EventSourceListener {
        @Override
        public void onOpen(EventSource eventSource, Response response) {
            Logger.info("SSE connection opened: " + response.code());
            reconnectAttempt = 0;
            notifyConnectionStatus(ConnectionStatus.CONNECTED);
            showNotification("SSE connection established", false);
        }

        @Override
        public void onEvent(EventSource eventSource, String id, String type, String data) {
            Logger.info("SSE Event Received: " + type + " - ID: " + id);
            if ("news-list".equals(type)) {
                processNewsListEvent(data, id);
            } else {
                Logger.info("Unknown event type: " + type);
            }
        }

        @Override
        public void onFailure(EventSource eventSource, Throwable t, Response response) {
            Logger.error("SSE Connection Error: " + (t != null ? t.getMessage() : "Unknown") +
                    " // code " + (response != null ? response.code() : "null"));
            notifyConnectionStatus(ConnectionStatus.ERROR);
            showNotification("SSE connection lost. Reconnecting...", true);
            reconnectToSSEServer();
        }

        @Override
        public void onClosed(EventSource eventSource) {
            Logger.info("SSE connection closed by server.");
            notifyConnectionStatus(ConnectionStatus.DISCONNECTED);
        }
    }
}
