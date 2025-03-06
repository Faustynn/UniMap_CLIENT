package org.main.unimap_pc.client.services;

import org.main.unimap_pc.client.configs.AppConfig;
import org.main.unimap_pc.client.utils.Logger;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService {
    private static final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final String CACHE_FILE = AppConfig.getCACHE_FILE();

    static {
        loadCache();
    }

    public static Object get(String key) {
        return cache.get(key);
    }

    public static void put(String key, Object value) {
        cache.put(key, value);
        if (PreferenceServise.containsKey("REMEMBER") == true) {
            saveCache();
        }
    }

    public static void remove(String key) {
        cache.remove(key);
        if (PreferenceServise.containsKey("REMEMBER") == true) {
            saveCache();
        }
    }

    public static boolean containsKey(String key) {
        return cache.containsKey(key);
    }


    private static void saveCache() {
        File file = new File(CACHE_FILE);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(cache);
        } catch (IOException e) {
            Logger.error("Error saving cache to file: " + e.getMessage());
        }
    }

    private static void loadCache() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) {
            saveCache();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, Object> loadedCache = (Map<String, Object>) ois.readObject();
            cache.putAll(loadedCache);
        } catch (IOException | ClassNotFoundException e) {
            Logger.error("Error loading cache: " + e.getMessage());
        }
    }

    public static void clearCache() {
        File file = new File(CACHE_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}