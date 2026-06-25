package com.eraoftribes.engine.asset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private final String assetDir;
    private final Map<String, Object> cache = new HashMap<>();
    private final Gson gson = new Gson();

    public AssetManager(String assetDir) {
        this.assetDir = assetDir;
        new File(assetDir).mkdirs();
        System.out.println("[AssetManager] Directory: " + assetDir);
    }

    public <T> T loadJSON(String path, Class<T> clazz) {
        var file = new File(assetDir, path);
        if (!file.exists()) {
            System.err.println("[AssetManager] File not found: " + file);
            return null;
        }
        try (var reader = new FileReader(file)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            System.err.println("[AssetManager] Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    public <T> T loadJSON(String path, java.lang.reflect.Type type) {
        var file = new File(assetDir, path);
        if (!file.exists()) return null;
        try (var reader = new FileReader(file)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("[AssetManager] Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    public String loadText(String path) {
        var file = new File(assetDir, path);
        if (!file.exists()) return null;
        try {
            return new String(java.nio.file.Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            return null;
        }
    }

    public void cache(String key, Object data) {
        cache.put(key, data);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCached(String key) {
        return (T) cache.get(key);
    }

    public void clearCache() { cache.clear(); }
}
