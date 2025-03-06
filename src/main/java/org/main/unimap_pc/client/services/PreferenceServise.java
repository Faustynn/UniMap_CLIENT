package org.main.unimap_pc.client.services;

import org.main.unimap_pc.client.configs.AppConfig;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.main.unimap_pc.client.utils.Logger;

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
        if (Boolean.TRUE.equals(PreferenceServise.get("REMEMBER"))) {
            savePreferences();
        }else {
            deletePreferences();
        }
    }

    public static void remove(String key) {
        prefs.remove(key);
        if (Boolean.TRUE.equals(PreferenceServise.get("REMEMBER"))) {
            savePreferences();
        } else {
            deletePreferences();
        }
    }

    public static boolean containsKey(String key) {
        return prefs.containsKey(key);
    }

    private static void savePreferences() {
        File file = new File(PREFS_FILE);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(prefs);
        } catch (IOException e) {
            Logger.error("Error saving preferences to file: " + e.getMessage());
        }
    }
    private static void loadPreferences() {
        File file = new File(PREFS_FILE);
        if (!file.exists()) {
            savePreferences(); // Create an empty preferences file if it does not exist
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Object> loadedPrefs = (Map<String, Object>) ois.readObject();
            prefs.putAll(loadedPrefs);
        } catch (IOException | ClassNotFoundException e) {
            Logger.error("Error loading preferences: " + e.getMessage());
        }
    }

    public static void deletePreferences() {
        File file = new File(PREFS_FILE);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Preferences file deleted successfully.");
            } else {
                System.err.println("Failed to delete preferences file.");
            }
        } else {
            System.out.println("Preferences file does not exist.");
        }
    }
}