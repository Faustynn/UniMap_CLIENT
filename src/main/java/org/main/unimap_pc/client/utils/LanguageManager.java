package org.main.unimap_pc.client.utils;

import lombok.Getter;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.main.unimap_pc.client.services.AuthService.prefs;

@Getter
public class LanguageManager {
    private static LanguageManager instance;
    @Getter
    private static ResourceBundle currentBundle;

    private LanguageManager() {
        changeLanguage("en");
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public static void changeLanguage(String language) {
        try {
            Locale locale = Locale.forLanguageTag(language);
            currentBundle = ResourceBundle.getBundle("org.main.unimap_pc.langs.lang", locale);
            prefs.put("LANGUAGE", language);
        } catch (Exception e) {
            Logger.error("Failed to load language resources for " + language + ": " + e.getMessage());
            if (!language.equals("en")) {
                System.out.println("Falling back to English");
                changeLanguage("en");
            }
        }
    }

    public void registerController(LanguageSupport controller) {
        controller.updateUILanguage(currentBundle);
    }
}