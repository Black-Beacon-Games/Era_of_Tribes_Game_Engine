package com.eraoftribes.engine;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    private final Map<String, ModuleConfig> modules = new HashMap<>();
    private final Map<String, Object> loadedModules = new HashMap<>();

    public void loadConfig(String path) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            ModulesWrapper wrapper = gson.fromJson(reader, ModulesWrapper.class);
            modules.putAll(wrapper.modules);
        }
    }

    public void initialize() {
        for (var entry : modules.entrySet()) {
            var config = entry.getValue();
            if (config.enabled) {
                System.out.println("[ModuleManager] Initializing module: " + entry.getKey());
                loadedModules.put(entry.getKey(), new Object());
            }
        }
    }

    public boolean isEnabled(String module) {
        var config = modules.get(module);
        return config != null && config.enabled;
    }

    @SuppressWarnings("unchecked")
    public <T> T getModule(String name) {
        return (T) loadedModules.get(name);
    }

    private static class ModulesWrapper {
        public Map<String, ModuleConfig> modules;
    }

    private static class ModuleConfig {
        public boolean enabled;
        public String path;
        public int priority;
    }
}
