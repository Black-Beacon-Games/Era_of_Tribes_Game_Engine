package com.eraoftribes.engine.script;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ScriptEngine {
    private final Map<String, String> scripts = new HashMap<>();

    public ScriptEngine() {
        System.out.println("[ScriptEngine] Initialized.");
    }

    public void loadScript(String name, String path) {
        var file = new File(path);
        if (!file.exists()) {
            System.err.println("[ScriptEngine] Script not found: " + path);
            return;
        }
        try {
            var content = Files.readString(file.toPath());
            scripts.put(name, content);
            System.out.println("[ScriptEngine] Loaded script: " + name);
        } catch (Exception e) {
            System.err.println("[ScriptEngine] Failed to load " + path + ": " + e.getMessage());
        }
    }

    public String getScript(String name) {
        return scripts.get(name);
    }

    public void execute(String name) {
        var script = scripts.get(name);
        if (script != null) {
            System.out.println("[ScriptEngine] Executing script: " + name);
        }
    }

    public void reloadAll() {}
}
