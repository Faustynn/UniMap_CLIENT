package org.main.unimap_pc.client.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreferenceServise {
    private static final Map<String, Object> prefs = new ConcurrentHashMap<>();

    public static Object get(String key) {
        return prefs.get(key);
    }

    public static void put(String key, Object value) {
        prefs.put(key, value);
    }

    public static void remove(String key) {
        prefs.remove(key);
    }

    public static boolean containsKey(String key) {
        return prefs.containsKey(key);
    }
}