package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.EngineConfig;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;

public class SettingsScene extends Scene {
    private final Engine engine;
    private boolean dirty;

    private static final String[][] RESOLUTIONS = {{"1280", "720"}, {"1600", "900"}, {"1920", "1080"}, {"2560", "1440"}};
    private static final int[] AA_LEVELS = {0, 2, 4, 8};
    private static final int[] INTERVALS = {30, 60, 120, 300, 600};

    private record Setting(String label, Runnable toggle) {}

    private Setting[] settings;

    public SettingsScene(Engine engine) {
        super("settings");
        this.engine = engine;
        this.dirty = true;
    }

    public void onEnter() {
        dirty = true;
        buildSettings();
    }

    private void buildSettings() {
        EngineConfig cfg = engine.getConfig();
        settings = new Setting[] {
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
            new Setting("Antialiasing: " + cfg.renderer.antialiasing + "x", () -> {
                int ai = 0;
                for (int i = 0; i < AA_LEVELS.length; i++) if (cfg.renderer.antialiasing == AA_LEVELS[i]) { ai = i; break; }
                ai = (ai + 1) % AA_LEVELS.length;
                cfg.renderer.antialiasing = AA_LEVELS[ai];
            }),
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
            new Setting("Mouse Sens:  " + cfg.input.mouseSensitivity, () -> {
                cfg.input.mouseSensitivity += 0.25;
                if (cfg.input.mouseSensitivity > 3.0) cfg.input.mouseSensitivity = 0.25;
            }),
            new Setting("Edge Scroll: " + (cfg.input.edgeScroll ? "ON" : "OFF"), () -> {
                cfg.input.edgeScroll = !cfg.input.edgeScroll;
            }),
            new Setting("Auto-Save:   " + cfg.saves.autosaveInterval + "s", () -> {
                int si = 0;
                for (int i = 0; i < INTERVALS.length; i++) if (cfg.saves.autosaveInterval == INTERVALS[i]) { si = i; break; }
                si = (si + 1) % INTERVALS.length;
                cfg.saves.autosaveInterval = INTERVALS[si];
            })
        };
    }

    public void update(double dt) {
        if (dirty) { buildSettings(); dirty = false; }
    }

    public void render(Renderer r) {
        int w = r.getWidth();
        int h = r.getHeight();
        int cx = w / 2;

        r.drawRect(0, 0, w, h, 0.05f, 0.05f, 0.08f, 1);

        r.drawTextCentered("SETTINGS", cx, 55, 0.8f, 0.6f, 0.2f, 1, r.getHeaderFont());

        int startY = 90;
        int rowH = 32;
        int btnW = w - 120;

        for (int i = 0; i < settings.length; i++) {
            int ry = startY + i * rowH;
            boolean hover = r.getMouseX() >= 60 && r.getMouseX() <= 60 + btnW
                && r.getMouseY() >= ry && r.getMouseY() <= ry + rowH;

            if (hover) r.drawRect(60, ry, btnW, rowH, 0.2f, 0.3f, 0.5f, 1);
            else r.drawRect(60, ry, btnW, rowH, 0.08f, 0.08f, 0.12f, 1);

            r.drawText(settings[i].label(), 70, ry + rowH / 2 + 5, 0.9f, 0.9f, 0.9f, 1, r.getBodyFont());

            if (hover && r.isMouseClicked()) {
                r.consumeClick();
                settings[i].toggle().run();
                dirty = true;
                return;
            }
        }

        int by = startY + settings.length * rowH + 20;
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
