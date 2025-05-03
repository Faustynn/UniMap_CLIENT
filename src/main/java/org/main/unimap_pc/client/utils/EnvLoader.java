package org.main.unimap_pc.client.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("src/main/resources/org/main/unimap_pc")
            .filename(".env")
            .load();

    public static String getGoogleClientId() {
        return dotenv.get("GOOGLE_CLIENT_ID");
    }

    public static String getGoogleClientSecret() {
        return dotenv.get("GOOGLE_CLIENT_SECRET");
    }

    public static String getFacebookClientId() {
        return dotenv.get("FACEBOOK_CLIENT_ID");
    }

    public static String getFacebookClientSecret() {
        return dotenv.get("FACEBOOK_CLIENT_SECRET");
    }


}