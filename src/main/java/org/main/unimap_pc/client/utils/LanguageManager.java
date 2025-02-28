package org.main.unimap_pc.client.utils;

import lombok.Getter;
import org.main.unimap_pc.client.services.PreferenceServise;

import java.util.Locale;
import java.util.ResourceBundle;

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
            PreferenceServise.put("LANGUAGE", language);
        } catch (Exception e) {
            System.err.println("Failed to load language resources for " + language + ": " + e.getMessage());
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