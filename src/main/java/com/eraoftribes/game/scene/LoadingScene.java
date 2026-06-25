package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;
import com.eraoftribes.game.Game;

public class LoadingScene extends Scene {
    private final Engine engine;
    private final Game game;
    private int step;
    private int maxSteps;
    private long lastTick;

    public LoadingScene(Engine engine, Game game) {
        super("loading");
        this.engine = engine;
        this.game = game;
        this.step = 0;
        this.maxSteps = 15;
        this.lastTick = 0;
    }

    public void onEnter() {
        step = 0;
        lastTick = System.currentTimeMillis();
        draw();
    }

    public void update(double dt) {
        long now = System.currentTimeMillis();
        if (now - lastTick < 180) return;
        lastTick = now;

        if (step >= maxSteps) {
            engine.getSceneManager().switchTo("main_menu");
            return;
        }

        String[] messages = {
            "Initializing engine core",
            "Loading configuration",
            "Starting modules",
            "Initializing renderer",
            "Setting up audio",
            "Loading input devices",
            "Preparing network",
            "Loading save system",
            "Loading UI framework",
            "Preparing script engine",
            "Loading game data - tribes",
            "Loading game data - technologies",
            "Loading game data - buildings and units",
            "Loading game data - resources",
            "Finalizing setup"
        };

        String msg = step < messages.length ? messages[step] : "Loading...";

        if (step == 10) game.getTribeManager().loadAll();
        if (step == 11) game.getTechManager().loadAll();
        if (step == 12) { game.getBuildingManager().loadAll(); game.getUnitManager().loadAll(); }
        if (step == 13) game.getResourceManager().loadAll();

        drawProgress(msg, step + 1, maxSteps);
        step++;
    }

    public void render(Renderer renderer) {}

    public void draw() {
        clearScreen();
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("         E R A   O F   T R I B E S");
        System.out.println("  ============================================");
        System.out.println();
    }

    private void drawProgress(String message, int current, int total) {
        int barWidth = 30;
        int filled = (current * barWidth) / total;
        int pct = (current * 100) / total;

        StringBuilder bar = new StringBuilder("  [");
        for (int i = 0; i < barWidth; i++) {
            if (i < filled) bar.append("=");
            else if (i == filled && current < total) bar.append(">");
            else bar.append(" ");
        }
        bar.append("]");

        System.out.println("  " + message + "...");
        System.out.println(bar.toString() + " " + pct + "%");
        System.out.println();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
