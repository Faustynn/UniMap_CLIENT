package org.main.unimap_pc.client.services;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.main.unimap_pc.client.configs.AppConfig;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class oAuth2ClientService {
    private String currentOAuth2Provider;
    private boolean isServerRunning = false;
    private BooleanProperty successfull = new SimpleBooleanProperty(false);

    public boolean isSuccessfull() {
        return successfull.get();
    }
    public BooleanProperty successfullProperty() {
        return successfull;
    }

    // Start local server
    public void startServer(String provider) {
        if (isServerRunning) {
            System.out.println("Server is already running.");
            return;
        }

        currentOAuth2Provider = provider;
        isServerRunning = true;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(3000)) {
            //    System.out.println("Server started on port 3000.");
                Socket socket = serverSocket.accept();
                handleRedirect(socket);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                isServerRunning = false;
            //    System.out.println("Server stopped.");
            }
        });
    }

    // Open authorization page for Google or Facebook
    public void openAuthorizationPage(String provider) {
        String authorizationUrl = getAuthorizationUrl(provider);
        try {
            Desktop.getDesktop().browse(new URI(authorizationUrl));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private String getAuthorizationUrl(String provider) {
        switch (provider) {
            case "google":
                return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + AppConfig.getGOOGLE_CLIENT_ID() +
                        "&redirect_uri=" + AppConfig.getOAUTH2_GOOGLE_REDIR() +
                        "&response_type=code&scope=email%20profile" +
                        "&access_type=offline";
            case "facebook":
                return "https://www.facebook.com/v12.0/dialog/oauth?client_id=" + AppConfig.getFACEBOOK_CLIENT_ID() +
                        "&redirect_uri=" + AppConfig.getOAUTH2_FACEBOOK_REDIR() +
                        "&response_type=code&scope=email";
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }

    // Handle redirect from OAuth2 provider
    private void handleRedirect(Socket socket) {
        try {
      //      System.out.println("Redirect received from OAuth2 provider.");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            String line;
            String requestLine = null;
            String code = null;

            // Read HTTP
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (requestLine == null) {
                    requestLine = line;
                }
            }

            if (requestLine != null && requestLine.contains("?code=")) {
                String query = requestLine.split(" ")[1]; // GET /redirect?code=xyz HTTP/1.1 -> /redirect?code=xyz
                code = extractCodeFromQuery(query);
            }

            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println();
            writer.println("<html><body>");
            writer.println("<h1>Authentication Successful</h1>");
            writer.println("<p>You can close this window now.</p>");
            writer.println("</body></html>");

            // Close conn.
            writer.close();
            reader.close();
            socket.close();

            if (code != null) {
                final String finalCode = code;
                System.out.println("Start sending token to server with code: " + finalCode);
                Platform.runLater(() -> sendTokenToServer(finalCode));
            } else {
                System.err.println("Authorization code not found in the response");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Extract code from query
    private String extractCodeFromQuery(String query) {
        if (query.contains("?code=")) {
            String codeSection = query.split("\\?code=")[1];
            if (codeSection.contains("&")) {
                return codeSection.split("&")[0];
            }
            return codeSection;
        }
        return null;
    }

    // Send token to server
    public void sendTokenToServer(String code) {
        String tokenUrl = AppConfig.getOAUTH2_LOGIN_URL();
        String postData = "code=" + code + "&provider=" + currentOAuth2Provider;
        String refreshToken = null;
        try {
            URL url = new URL(tokenUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            //    System.out.println("Response Code: " + responseCode);

            String setCookieHeader = connection.getHeaderField("Set-Cookie");
            if (setCookieHeader != null) {
                for (String cookie : setCookieHeader.split(";")) {
                    if (cookie.trim().startsWith("refreshToken=")) {
                        refreshToken = cookie.trim().substring("refreshToken=".length());
                        break;
                    }
                }
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    responseCode == 200 ? connection.getInputStream() : connection.getErrorStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                if (responseCode == 200) {
              //      System.out.println("Authentication successful!");
               //     System.out.println("Response: " + response.toString());
                    AuthService.handleSuccessfulAuth2Login(response.toString(), refreshToken);
                 //   System.out.println("Setting successfull property to true");
                    Platform.runLater(() -> {
                        this.successfull.set(true);
                    });
                } else {
                    System.err.println("Authentication failed: " + response.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}