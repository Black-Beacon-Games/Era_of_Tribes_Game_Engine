package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.EngineConfig;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;
import java.util.Scanner;

public class SettingsScene extends Scene {
    private final Engine engine;
    private final Scanner scanner;
    private boolean waiting;

    public SettingsScene(Engine engine) {
        super("settings");
        this.engine = engine;
        this.scanner = new Scanner(System.in);
        this.waiting = false;
    }

    public void onEnter() {
        waiting = true;
        draw();
        listenInput();
    }

    public void onLeave() {
        waiting = false;
    }

    public void update(double dt) {}

    public void render(Renderer renderer) {}

    private void draw() {
        clearScreen();
        EngineConfig cfg = engine.getConfig();
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("                S E T T I N G S");
        System.out.println("  ============================================");
        System.out.println();
        System.out.println("  --- Display ---");
        System.out.println("  [1]  Resolution:   " + cfg.renderer.resolution.width + "x" + cfg.renderer.resolution.height);
        System.out.println("  [2]  Fullscreen:   " + (cfg.renderer.fullscreen ? "ON" : "OFF"));
        System.out.println("  [3]  VSync:        " + (cfg.renderer.vsync ? "ON" : "OFF"));
        System.out.println("  [4]  Antialiasing: " + cfg.renderer.antialiasing + "x");
        System.out.println();
        System.out.println("  --- Audio ---");
        System.out.println("  [5]  Master Volume: " + (int)(cfg.audio.masterVolume * 100) + "%");
        System.out.println("  [6]  Music Volume:  " + (int)(cfg.audio.musicVolume * 100) + "%");
        System.out.println("  [7]  SFX Volume:    " + (int)(cfg.audio.sfxVolume * 100) + "%");
        System.out.println();
        System.out.println("  --- Input ---");
        System.out.println("  [8]  Mouse Sensitivity: " + cfg.input.mouseSensitivity);
        System.out.println("  [9]  Edge Scroll:       " + (cfg.input.edgeScroll ? "ON" : "OFF"));
        System.out.println();
        System.out.println("  --- Game ---");
        System.out.println("  [10] Auto-Save Interval: " + cfg.saves.autosaveInterval + "s");
        System.out.println();
        System.out.println("  [0]  Back to Main Menu");
        System.out.println();
        System.out.print("  Select setting to toggle: ");
    }

    private void listenInput() {
        while (waiting && scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!waiting) return;

            EngineConfig cfg = engine.getConfig();

            switch (line) {
                case "0" -> {
                    waiting = false;
                    engine.getSceneManager().switchTo("main_menu");
                    return;
                }
                case "1" -> {
                    String[][] resolutions = {{"1280", "720"}, {"1600", "900"}, {"1920", "1080"}, {"2560", "1440"}};
                    int ri = -1;
                    for (int i = 0; i < resolutions.length; i++) {
                        int w = Integer.parseInt(resolutions[i][0]);
                        int h = Integer.parseInt(resolutions[i][1]);
                        if (cfg.renderer.resolution.width == w && cfg.renderer.resolution.height == h) {
                            ri = i;
                            break;
                        }
                    }
                    ri = (ri + 1) % resolutions.length;
                    cfg.renderer.resolution.width = Integer.parseInt(resolutions[ri][0]);
                    cfg.renderer.resolution.height = Integer.parseInt(resolutions[ri][1]);
                    System.out.println("  Resolution set to " + cfg.renderer.resolution.width + "x" + cfg.renderer.resolution.height);
                }
                case "2" -> { cfg.renderer.fullscreen = !cfg.renderer.fullscreen; System.out.println("  Fullscreen: " + (cfg.renderer.fullscreen ? "ON" : "OFF")); }
                case "3" -> { cfg.renderer.vsync = !cfg.renderer.vsync; System.out.println("  VSync: " + (cfg.renderer.vsync ? "ON" : "OFF")); }
                case "4" -> {
                    int[] levels = {0, 2, 4, 8};
                    int ai = 0;
                    for (int i = 0; i < levels.length; i++) {
                        if (cfg.renderer.antialiasing == levels[i]) { ai = i; break; }
                    }
                    ai = (ai + 1) % levels.length;
                    cfg.renderer.antialiasing = levels[ai];
                    System.out.println("  Antialiasing: " + cfg.renderer.antialiasing + "x");
                }
                case "5" -> {
                    double v = (int)(cfg.audio.masterVolume * 10 + 1) / 10.0;
                    if (v > 1.0) v = 0.0;
                    cfg.audio.masterVolume = v;
                    System.out.println("  Master Volume: " + (int)(v * 100) + "%");
                }
                case "6" -> {
                    double v = (int)(cfg.audio.musicVolume * 10 + 1) / 10.0;
                    if (v > 1.0) v = 0.0;
                    cfg.audio.musicVolume = v;
                    System.out.println("  Music Volume: " + (int)(v * 100) + "%");
                }
                case "7" -> {
                    double v = (int)(cfg.audio.sfxVolume * 10 + 1) / 10.0;
                    if (v > 1.0) v = 0.0;
                    cfg.audio.sfxVolume = v;
                    System.out.println("  SFX Volume: " + (int)(v * 100) + "%");
                }
                case "8" -> {
                    cfg.input.mouseSensitivity += 0.25;
                    if (cfg.input.mouseSensitivity > 3.0) cfg.input.mouseSensitivity = 0.25;
                    System.out.println("  Mouse Sensitivity: " + cfg.input.mouseSensitivity);
                }
                case "9" -> { cfg.input.edgeScroll = !cfg.input.edgeScroll; System.out.println("  Edge Scroll: " + (cfg.input.edgeScroll ? "ON" : "OFF")); }
                case "10" -> {
                    int[] intervals = {30, 60, 120, 300, 600};
                    int si = 0;
                    for (int i = 0; i < intervals.length; i++) {
                        if (cfg.saves.autosaveInterval == intervals[i]) { si = i; break; }
                    }
                    si = (si + 1) % intervals.length;
                    cfg.saves.autosaveInterval = intervals[si];
                    System.out.println("  Auto-Save Interval: " + cfg.saves.autosaveInterval + "s");
                }
                default -> { System.out.print("  Invalid option. Try again: "); continue; }
            }

            System.out.println();
            System.out.print("  Press Enter to continue...");
            scanner.nextLine();
            draw();
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
