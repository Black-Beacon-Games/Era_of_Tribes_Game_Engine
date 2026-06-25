package com.eraoftribes.engine;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EngineConfig {
    public RendererConfig renderer;
    public AudioConfig audio;
    public InputConfig input;
    public MultiplayerConfig multiplayer;
    public LocalizationConfig localization;
    public SavesConfig saves;
    public DebugConfig debug;
    public String version;

    public static EngineConfig load(String path) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, EngineConfig.class);
        }
    }

    public static class RendererConfig {
        public String backend;
        public boolean vsync;
        public int targetFps;
        public ResolutionConfig resolution;
        public boolean fullscreen;
        public boolean borderless;
        public int antialiasing;
        public int anisotropicFiltering;
    }

    public static class ResolutionConfig {
        public int width;
        public int height;
    }

    public static class AudioConfig {
        public double masterVolume;
        public double musicVolume;
        public double sfxVolume;
        public double ambientVolume;
        public double voiceVolume;
    }

    public static class InputConfig {
        public double mouseSensitivity;
        public double scrollZoomSpeed;
        public boolean edgeScroll;
        public double edgeScrollSpeed;
        public boolean controllerSupport;
        public boolean touchSupport;
    }

    public static class MultiplayerConfig {
        public int maxPlayers;
        public int timeout;
        public int reconnectAttempts;
        public boolean natTraversal;
        public int serverTickRate;
    }

    public static class LocalizationConfig {
        public String defaultLocale;
        public String fallback;
    }

    public static class SavesConfig {
        public int autosaveInterval;
        public int maxSaveSlots;
        public boolean cloudSync;
        public boolean compression;
    }

    public static class DebugConfig {
        public boolean console;
        public boolean fpsOverlay;
        public String logLevel;
        public boolean profiling;
    }
}
