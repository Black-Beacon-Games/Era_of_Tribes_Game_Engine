package com.eraoftribes.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EngineConfig {
    public String engine = "EraEngine";
    public String version = "0.0.3";
    public String game = "Era of Tribes";
    public RendererConfig renderer = new RendererConfig();
    public AudioConfig audio = new AudioConfig();
    public InputConfig input = new InputConfig();
    public MultiplayerConfig multiplayer = new MultiplayerConfig();
    public LocalizationConfig localization = new LocalizationConfig();
    public SavesConfig saves = new SavesConfig();
    public DebugConfig debug = new DebugConfig();

    public transient String configPath;

    public static EngineConfig load(String path) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            EngineConfig cfg = gson.fromJson(reader, EngineConfig.class);
            cfg.configPath = path;
            if (cfg.renderer == null) cfg.renderer = new RendererConfig();
            if (cfg.audio == null) cfg.audio = new AudioConfig();
            if (cfg.input == null) cfg.input = new InputConfig();
            if (cfg.multiplayer == null) cfg.multiplayer = new MultiplayerConfig();
            if (cfg.localization == null) cfg.localization = new LocalizationConfig();
            if (cfg.saves == null) cfg.saves = new SavesConfig();
            if (cfg.debug == null) cfg.debug = new DebugConfig();
            return cfg;
        }
    }

    public void save() {
        if (configPath == null) return;
        try (FileWriter writer = new FileWriter(configPath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
        } catch (IOException e) {
            System.err.println("[Config] Failed to save config: " + e.getMessage());
        }
    }

    public static class RendererConfig {
        public String backend = "swing";
        public boolean vsync = true;
        public int targetFps = 60;
        public ResolutionConfig resolution = new ResolutionConfig();
        public boolean fullscreen;
        public boolean borderless = true;
        public int antialiasing = 4;
        public int anisotropicFiltering = 8;
    }

    public static class ResolutionConfig {
        public int width = 1920;
        public int height = 1080;
    }

    public static class AudioConfig {
        public double masterVolume = 1.0;
        public double musicVolume = 0.8;
        public double sfxVolume = 1.0;
        public double ambientVolume = 0.5;
        public double voiceVolume = 1.0;
    }

    public static class InputConfig {
        public double mouseSensitivity = 1.0;
        public double scrollZoomSpeed = 1.0;
        public boolean edgeScroll = true;
        public double edgeScrollSpeed = 3.0;
        public boolean controllerSupport;
        public boolean touchSupport;
    }

    public static class MultiplayerConfig {
        public int maxPlayers = 8;
        public int timeout = 30000;
        public int reconnectAttempts = 5;
        public boolean natTraversal = true;
        public int serverTickRate = 20;
    }

    public static class LocalizationConfig {
        public String defaultLocale = "de";
        public String fallback = "en";
    }

    public static class SavesConfig {
        public int autosaveInterval = 180;
        public int maxSaveSlots = 30;
        public boolean cloudSync;
        public boolean compression = true;
    }

    public static class DebugConfig {
        public boolean console;
        public boolean fpsOverlay;
        public boolean profiling;
        public boolean showMemory;
        public boolean showVersion;
        public boolean showCoordinates;
        public String logLevel = "info";
    }
}
