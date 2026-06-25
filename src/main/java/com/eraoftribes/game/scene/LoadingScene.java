package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;
import com.eraoftribes.game.Game;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoadingScene extends Scene {
    private final Engine engine;
    private final Game game;
    private int step;
    private int maxSteps;
    private long lastTick;
    private int bgTextureId;
    private boolean bgLoaded;

    private final List<String> quotes = new ArrayList<>();
    private String currentQuote;
    private long quoteSwitchTime;
    private static final long QUOTE_INTERVAL = 5000;
    private final Random random = new Random();

    private static final String[] MESSAGES = {
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

    public LoadingScene(Engine engine, Game game) {
        super("loading");
        this.engine = engine;
        this.game = game;
        this.step = 0;
        this.maxSteps = 15;
        this.lastTick = 0;
        this.bgTextureId = 0;
        this.bgLoaded = false;
        this.currentQuote = "";
        this.quoteSwitchTime = 0;
    }

    public void onEnter() {
        step = 0;
        lastTick = System.currentTimeMillis();
        quoteSwitchTime = System.currentTimeMillis();
        bgLoaded = false;
        loadBackground();
        loadQuotes();
        pickRandomQuote();
    }

    private void loadBackground() {
        File bgDir = new File("assets/loadingscreen/background");
        if (bgDir.isDirectory()) {
            File[] files = bgDir.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg")
                    || lower.endsWith(".jpeg") || lower.endsWith(".bmp");
            });
            if (files != null && files.length > 0) {
                bgTextureId = engine.getRenderer().loadTexture(files[0].getAbsolutePath());
                if (bgTextureId != 0) bgLoaded = true;
            }
        }
        if (!bgLoaded) {
            System.out.println("[LoadingScreen] No background image found in assets/loadingscreen/background/");
        }
    }

    private void loadQuotes() {
        quotes.clear();
        File qf = new File("game/config/loading_quotes.txt");
        if (!qf.exists()) qf = new File(engine.getGamePath() + "/config/loading_quotes.txt");
        if (qf.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(qf))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        if (line.startsWith("\"") && line.endsWith("\"")) {
                            line = line.substring(1, line.length() - 1);
                        }
                        quotes.add(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[LoadingScreen] Failed to load quotes: " + e.getMessage());
            }
        }
        if (quotes.isEmpty()) {
            quotes.add("Building a civilization...");
        }
        System.out.println("[LoadingScreen] Loaded " + quotes.size() + " quotes.");
    }

    private void pickRandomQuote() {
        if (!quotes.isEmpty()) {
            currentQuote = quotes.get(random.nextInt(quotes.size()));
        }
        quoteSwitchTime = System.currentTimeMillis();
    }

    public void update(double dt) {
        long now = System.currentTimeMillis();

        if (now - quoteSwitchTime >= QUOTE_INTERVAL) {
            pickRandomQuote();
        }

        if (now - lastTick < 200) return;
        lastTick = now;

        if (step >= maxSteps) {
            engine.getSceneManager().switchTo("main_menu");
            return;
        }

        if (step == 10) game.getTribeManager().loadAll();
        if (step == 11) game.getTechManager().loadAll();
        if (step == 12) { game.getBuildingManager().loadAll(); game.getUnitManager().loadAll(); }
        if (step == 13) game.getResourceManager().loadAll();

        step++;
    }

    public void render(Renderer r) {
        int w = r.getWidth();
        int h = r.getHeight();

        if (bgLoaded) {
            r.drawTextureFull(bgTextureId);
            r.drawRect(0, 0, w, h, 0, 0, 0, 0.5f);
        } else {
            r.drawRect(0, 0, w, h, 0.05f, 0.05f, 0.08f, 1);
        }

        r.drawTextCentered("ERA OF TRIBES", w / 2, (int) (h * 0.28), 0.8f, 0.6f, 0.2f, 1, r.getTitleFont());

        int barY = (int) (h * 0.5);
        int barH = 24;
        int barW = (int) (w * 0.55f);
        int barX = (w - barW) / 2;

        r.drawRect(barX, barY, barW, barH, 0.1f, 0.1f, 0.1f, 0.8f);
        int filled = (step * barW) / maxSteps;
        r.drawRect(barX, barY, filled, barH, 0.3f, 0.5f, 0.8f, 1);

        int pct = (step * 100) / maxSteps;
        r.drawTextCentered(pct + "%", w / 2, barY + barH / 2, 1, 1, 1, 1);

        String msg = step < MESSAGES.length ? MESSAGES[step] : "Loading...";
        r.drawTextCentered(msg, w / 2, barY + barH + 30, 0.7f, 0.7f, 0.7f, 1, r.getSmallFont());

        if (currentQuote != null && !currentQuote.isEmpty()) {
            r.drawTextCentered("\u201C" + currentQuote + "\u201D", w / 2, (int) (h * 0.7), 0.6f, 0.6f, 0.6f, 1, r.getSmallFont());
        }
    }
}
