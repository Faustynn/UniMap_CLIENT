package org.main.unimap_pc.client.services;

import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreferenceServise {
    private static final Map<String, Object> prefs = new ConcurrentHashMap<>();
    private static final String PREFS_FILE = AppConfig.getPREFS_FILE();

    static {
        loadPreferences();
    }

    public static Object get(String key) {
        return prefs.get(key);
    }

    public static void put(String key, Object value) {
        prefs.put(key, value);
        if (Boolean.TRUE.equals(get("REMEMBER"))) {
            savePreferences();
        } else {
            deletePreferences();
        }
    }

    public static void remove(String key) {
        prefs.remove(key);
        if (Boolean.TRUE.equals(get("REMEMBER"))) {
            savePreferences();
        } else {
            deletePreferences();
        }
    }

    public static boolean containsKey(String key) {
        return prefs.containsKey(key);
    }

    private static void savePreferences() {
        ensureDirectoryExists();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PREFS_FILE))) {
            oos.writeObject(prefs);
        } catch (IOException e) {
            Logger.error("Error saving preferences to file: " + e.getMessage());
        }
    }

    private static void loadPreferences() {
        File file = new File(PREFS_FILE);
        if (!file.exists()) {
            savePreferences();
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object readObject = ois.readObject();
            if (readObject instanceof Map<?, ?> map) {
                prefs.putAll((Map<String, Object>) map);
            }
        } catch (IOException | ClassNotFoundException e) {
            Logger.error("Error loading preferences: " + e.getMessage());
        }
    }

    public static void deletePreferences() {
        File file = new File(PREFS_FILE);
        if (file.exists() && file.delete()) {
            Logger.info("Preferences file deleted successfully.");
        } else {
            Logger.info("Preferences file not found or failed to delete.");
        }
    }

    private static void ensureDirectoryExists() {
        File file = new File(PREFS_FILE);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            Logger.error("Failed to create preferences directory: " + parentDir);
        }
    }
}
