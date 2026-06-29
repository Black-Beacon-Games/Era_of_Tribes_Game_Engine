package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.EngineConfig;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;

public class SettingsScene extends Scene {
    private final Engine engine;
    private boolean dirty;
    private int activeTab;

    private static final String[][] RESOLUTIONS = {{"1280", "720"}, {"1600", "900"}, {"1920", "1080"}, {"2560", "1440"}};
    private static final int[] AA_LEVELS = {0, 2, 4, 8};
    private static final int[] ANISO_LEVELS = {0, 2, 4, 8, 16};
    private static final int[] FPS_VALUES = {30, 60, 120, 144, 240, 0};
    private static final int[] INTERVALS = {30, 60, 120, 300, 600};
    private static final String[] LOG_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"};

    private record Setting(String label, Runnable toggle) {}

    private static final String[] TAB_NAMES = {"Graphics", "Audio", "Input", "Game", "Debug"};
    private Setting[][] tabSettings;

    public SettingsScene(Engine engine) {
        super("settings");
        this.engine = engine;
        this.dirty = true;
        this.activeTab = 0;
    }

    public void onEnter() {
        dirty = true;
    }

    private void buildSettings() {
        EngineConfig cfg = engine.getConfig();
        tabSettings = new Setting[TAB_NAMES.length][];

        tabSettings[0] = new Setting[] {
            new Setting("Resolution:  " + cfg.renderer.resolution.width + "x" + cfg.renderer.resolution.height, () -> {
                int ri = findResIndex(cfg.renderer.resolution.width, cfg.renderer.resolution.height);
                ri = (ri + 1) % RESOLUTIONS.length;
                cfg.renderer.resolution.width = Integer.parseInt(RESOLUTIONS[ri][0]);
                cfg.renderer.resolution.height = Integer.parseInt(RESOLUTIONS[ri][1]);
            }),
            new Setting("Fullscreen:  " + (cfg.renderer.fullscreen ? "ON" : "OFF"), () -> {
                cfg.renderer.fullscreen = !cfg.renderer.fullscreen;
            }),
            new Setting("VSync:       " + (cfg.renderer.vsync ? "ON" : "OFF"), () -> {
                cfg.renderer.vsync = !cfg.renderer.vsync;
            }),
            new Setting("Borderless:  " + (cfg.renderer.borderless ? "ON" : "OFF"), () -> {
                cfg.renderer.borderless = !cfg.renderer.borderless;
            }),
            new Setting("Antialiasing: " + cfg.renderer.antialiasing + "x", () -> {
                int ai = 0;
                int[] levels = AA_LEVELS;
                for (int i = 0; i < levels.length; i++) if (cfg.renderer.antialiasing == levels[i]) { ai = i; break; }
                ai = (ai + 1) % levels.length;
                cfg.renderer.antialiasing = levels[ai];
            }),
            new Setting("Aniso Filter: " + cfg.renderer.anisotropicFiltering + "x", () -> {
                int ai = 0;
                for (int i = 0; i < ANISO_LEVELS.length; i++) if (cfg.renderer.anisotropicFiltering == ANISO_LEVELS[i]) { ai = i; break; }
                ai = (ai + 1) % ANISO_LEVELS.length;
                cfg.renderer.anisotropicFiltering = ANISO_LEVELS[ai];
            }),
            new Setting("Target FPS:   " + (cfg.renderer.targetFps == 0 ? "UNLIMITED" : cfg.renderer.targetFps), () -> {
                int fi = 0;
                for (int i = 0; i < FPS_VALUES.length; i++) if (cfg.renderer.targetFps == FPS_VALUES[i]) { fi = i; break; }
                fi = (fi + 1) % FPS_VALUES.length;
                cfg.renderer.targetFps = FPS_VALUES[fi];
            })
        };

        tabSettings[1] = new Setting[] {
            new Setting("Master Vol:  " + (int)(cfg.audio.masterVolume * 100) + "%", () -> {
                double v = (int)(cfg.audio.masterVolume * 10 + 1) / 10.0;
                cfg.audio.masterVolume = v > 1.0 ? 0.0 : v;
            }),
            new Setting("Music Vol:   " + (int)(cfg.audio.musicVolume * 100) + "%", () -> {
                double v = (int)(cfg.audio.musicVolume * 10 + 1) / 10.0;
                cfg.audio.musicVolume = v > 1.0 ? 0.0 : v;
            }),
            new Setting("SFX Vol:     " + (int)(cfg.audio.sfxVolume * 100) + "%", () -> {
                double v = (int)(cfg.audio.sfxVolume * 10 + 1) / 10.0;
                cfg.audio.sfxVolume = v > 1.0 ? 0.0 : v;
            }),
            new Setting("Ambient Vol: " + (int)(cfg.audio.ambientVolume * 100) + "%", () -> {
                double v = (int)(cfg.audio.ambientVolume * 10 + 1) / 10.0;
                cfg.audio.ambientVolume = v > 1.0 ? 0.0 : v;
            }),
            new Setting("Voice Vol:   " + (int)(cfg.audio.voiceVolume * 100) + "%", () -> {
                double v = (int)(cfg.audio.voiceVolume * 10 + 1) / 10.0;
                cfg.audio.voiceVolume = v > 1.0 ? 0.0 : v;
            })
        };

        tabSettings[2] = new Setting[] {
            new Setting("Mouse Sens:  " + String.format("%.2f", cfg.input.mouseSensitivity), () -> {
                cfg.input.mouseSensitivity += 0.25;
                if (cfg.input.mouseSensitivity > 3.0) cfg.input.mouseSensitivity = 0.25;
            }),
            new Setting("Edge Scroll: " + (cfg.input.edgeScroll ? "ON" : "OFF"), () -> {
                cfg.input.edgeScroll = !cfg.input.edgeScroll;
            }),
            new Setting("Scroll Speed: " + String.format("%.1f", cfg.input.scrollZoomSpeed), () -> {
                cfg.input.scrollZoomSpeed += 0.5;
                if (cfg.input.scrollZoomSpeed > 5.0) cfg.input.scrollZoomSpeed = 0.5;
            }),
            new Setting("Edge Speed:  " + String.format("%.1f", cfg.input.edgeScrollSpeed), () -> {
                cfg.input.edgeScrollSpeed += 0.5;
                if (cfg.input.edgeScrollSpeed > 5.0) cfg.input.edgeScrollSpeed = 0.5;
            }),
            new Setting("Controller:  " + (cfg.input.controllerSupport ? "ON" : "OFF"), () -> {
                cfg.input.controllerSupport = !cfg.input.controllerSupport;
            }),
            new Setting("Touch:       " + (cfg.input.touchSupport ? "ON" : "OFF"), () -> {
                cfg.input.touchSupport = !cfg.input.touchSupport;
            })
        };

        tabSettings[3] = new Setting[] {
            new Setting("Auto-Save:   " + cfg.saves.autosaveInterval + "s", () -> {
                int si = 0;
                for (int i = 0; i < INTERVALS.length; i++) if (cfg.saves.autosaveInterval == INTERVALS[i]) { si = i; break; }
                si = (si + 1) % INTERVALS.length;
                cfg.saves.autosaveInterval = INTERVALS[si];
            }),
            new Setting("Max Slots:   " + cfg.saves.maxSaveSlots, () -> {
                cfg.saves.maxSaveSlots += 5;
                if (cfg.saves.maxSaveSlots > 50) cfg.saves.maxSaveSlots = 5;
            }),
            new Setting("Cloud Sync:  " + (cfg.saves.cloudSync ? "ON" : "OFF"), () -> {
                cfg.saves.cloudSync = !cfg.saves.cloudSync;
            }),
            new Setting("Compression: " + (cfg.saves.compression ? "ON" : "OFF"), () -> {
                cfg.saves.compression = !cfg.saves.compression;
            })
        };

        tabSettings[4] = new Setting[] {
            new Setting("Console:     " + (cfg.debug.console ? "ON" : "OFF"), () -> {
                cfg.debug.console = !cfg.debug.console;
            }),
            new Setting("FPS Overlay: " + (cfg.debug.fpsOverlay ? "ON" : "OFF"), () -> {
                cfg.debug.fpsOverlay = !cfg.debug.fpsOverlay;
            }),
            new Setting("Profiling:   " + (cfg.debug.profiling ? "ON" : "OFF"), () -> {
                cfg.debug.profiling = !cfg.debug.profiling;
            }),
            new Setting("Show Memory: " + (cfg.debug.showMemory ? "ON" : "OFF"), () -> {
                cfg.debug.showMemory = !cfg.debug.showMemory;
            }),
            new Setting("Show Ver:    " + (cfg.debug.showVersion ? "ON" : "OFF"), () -> {
                cfg.debug.showVersion = !cfg.debug.showVersion;
            }),
            new Setting("Show Coords: " + (cfg.debug.showCoordinates ? "ON" : "OFF"), () -> {
                cfg.debug.showCoordinates = !cfg.debug.showCoordinates;
            }),
            new Setting("Log Level:   " + cfg.debug.logLevel, () -> {
                int li = 0;
                for (int i = 0; i < LOG_LEVELS.length; i++) if (LOG_LEVELS[i].equals(cfg.debug.logLevel)) { li = i; break; }
                li = (li + 1) % LOG_LEVELS.length;
                cfg.debug.logLevel = LOG_LEVELS[li];
            })
        };
    }

    public void update(double dt) {
        if (!dirty) return;
        buildSettings();
        dirty = false;
    }

    public void render(Renderer r) {
        int w = r.getWidth();
        int h = r.getHeight();
        int cx = w / 2;

        r.drawRect(0, 0, w, h, 0.05f, 0.05f, 0.08f, 1);
        r.drawTextCentered("SETTINGS", cx, 45, 0.8f, 0.6f, 0.2f, 1, r.getHeaderFont());

        int tabY = 70;
        int tabH = 34;
        int tabGap = 6;
        int perTabW = (w - 80 - tabGap * (TAB_NAMES.length - 1)) / TAB_NAMES.length;
        if (perTabW < 100) perTabW = 100;
        int totalW = perTabW * TAB_NAMES.length + tabGap * (TAB_NAMES.length - 1);
        int tabStartX = (w - totalW) / 2;

        for (int t = 0; t < TAB_NAMES.length; t++) {
            int tx = tabStartX + t * (perTabW + tabGap);
            boolean hover = r.getMouseX() >= tx && r.getMouseX() <= tx + perTabW
                && r.getMouseY() >= tabY && r.getMouseY() <= tabY + tabH;

            if (t == activeTab) {
                r.drawRect(tx, tabY, perTabW, tabH, 0.3f, 0.55f, 0.9f, 1);
            } else if (hover) {
                r.drawRect(tx, tabY, perTabW, tabH, 0.2f, 0.3f, 0.5f, 1);
            } else {
                r.drawRect(tx, tabY, perTabW, tabH, 0.08f, 0.08f, 0.12f, 1);
            }

            r.drawTextCentered(TAB_NAMES[t], tx + perTabW / 2, tabY + tabH / 2 + 4, 0.9f, 0.9f, 0.9f, 1, r.getBodyFont());

            if (hover && r.isMouseClicked()) {
                r.consumeClick();
                if (t != activeTab) {
                    activeTab = t;
                    dirty = true;
                    return;
                }
            }
        }

        Setting[] sets = tabSettings[activeTab];
        int startY = tabY + tabH + 15;
        int rowH = 34;
        int btnW = w - 160;
        int btnX = 80;

        for (int i = 0; i < sets.length; i++) {
            int ry = startY + i * rowH;
            boolean hover = r.getMouseX() >= btnX && r.getMouseX() <= btnX + btnW
                && r.getMouseY() >= ry && r.getMouseY() <= ry + rowH;

            if (hover) r.drawRect(btnX, ry, btnW, rowH, 0.2f, 0.3f, 0.5f, 1);
            else r.drawRect(btnX, ry, btnW, rowH, 0.06f, 0.06f, 0.1f, 1);

            r.drawText(sets[i].label(), btnX + 10, ry + rowH / 2 + 5, 0.9f, 0.9f, 0.9f, 1, r.getBodyFont());

            if (hover && r.isMouseClicked()) {
                r.consumeClick();
                sets[i].toggle().run();
                engine.getConfig().save();
                if (activeTab == 0) {
                    engine.getRenderer().applyConfig(engine.getConfig().renderer);
                }
                dirty = true;
                return;
            }
        }

        int by = startY + sets.length * rowH + 20;
        if (r.drawButton("Back to Main Menu", cx - 120, by, 240, 45)) {
            engine.getSceneManager().switchTo("main_menu");
        }
    }

    private int findResIndex(int w, int h) {
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            if (Integer.parseInt(RESOLUTIONS[i][0]) == w && Integer.parseInt(RESOLUTIONS[i][1]) == h) return i;
        }
        return 2;
    }
}
