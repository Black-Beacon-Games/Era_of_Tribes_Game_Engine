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

    private int bgTex;
    private int logoTex;
    private boolean assetsReady;

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
        this.bgTex = 0;
        this.logoTex = 0;
        this.assetsReady = false;
        this.currentQuote = "";
        this.quoteSwitchTime = 0;
    }

    public void onEnter() {
        step = 0;
        lastTick = System.currentTimeMillis();
        quoteSwitchTime = System.currentTimeMillis();
        assetsReady = false;
        loadAssets();
        loadQuotes();
        pickRandomQuote();
        startMusic();
    }

    public void onLeave() {
        engine.getAudio().stop("loading");
    }

    private void startMusic() {
        String base = engine.getGamePath() + "/audio/";
        engine.getAudio().loadTrack("loading", base + "loading.mp3", true, 0.6);
        engine.getAudio().play("loading");
    }

    private void loadAssets() {
        var r = engine.getRenderer();
        String base = "assets/loadingscreen/";

        bgTex = r.loadTexture(base + "background.png");
        if (bgTex == 0) bgTex = r.loadTexture(base + "background.jpg");
        if (bgTex == 0) bgTex = r.loadTexture(base + "background.jpeg");

        logoTex = r.loadTexture(base + "logo.svg",
            r.getWidth() > 0 ? r.getWidth() / 2 : 400, 120);

        assetsReady = bgTex != 0;
        System.out.println("[LoadingScreen] Assets loaded: bg=" + (bgTex != 0)
            + " logo=" + (logoTex != 0));
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
        if (quotes.isEmpty()) quotes.add("Building a civilization...");
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

        if (assetsReady) {
            r.drawTextureCover(bgTex);
            r.drawRect(0, 0, w, h, 0, 0, 0, 0.45f);
        } else {
            r.drawRect(0, 0, w, h, 0.04f, 0.04f, 0.07f, 1);
        }

        int logoW = (int) (w * 0.5f);
        int logoH = (int) (logoW * 0.3f);
        int logoX = (w - logoW) / 2;
        int logoY = (int) (h * 0.12);

        if (logoTex != 0) {
            r.drawTexture(logoTex, logoX, logoY, logoW, logoH);
        } else {
            r.drawTextCentered("ERA OF TRIBES", w / 2, (int) (h * 0.22), 0.85f, 0.65f, 0.2f, 1, r.getTitleFont());
        }

        int barY = (int) (h * 0.48);
        int barH = 28;
        int barW = (int) (w * 0.5f);
        int barX = (w - barW) / 2;
        int filled = (step * barW) / maxSteps;

        r.drawRect(barX, barY, barW, barH, 0.12f, 0.12f, 0.15f, 0.95f);
        r.drawRect(barX + 1, barY + 1, barW - 2, barH - 2, 0.2f, 0.2f, 0.25f, 1);
        r.drawRect(barX + 2, barY + 2, filled - 4, barH - 4, 0.28f, 0.55f, 0.9f, 1);

        for (int i = 0; i < 3; i++) {
            int shX = barX + (int) ((float) step / maxSteps * barW * 0.3f) + i * 30;
            int shY = barY + 3;
            r.drawRect(shX, shY, 4, barH - 6, 1, 1, 1, 0.15f);
        }

        int pct = (step * 100) / maxSteps;
        r.drawTextCentered(pct + "%", w / 2, barY + barH / 2, 1, 1, 1, 1);

        String msg = step < MESSAGES.length ? MESSAGES[step] : "Loading...";
        r.drawTextCentered(msg, w / 2, barY + barH + 28, 0.7f, 0.7f, 0.7f, 1, r.getSmallFont());

        if (currentQuote != null && !currentQuote.isEmpty()) {
            r.drawTextCentered("\u201C" + currentQuote + "\u201D", w / 2, (int) (h * 0.68), 0.6f, 0.6f, 0.6f, 1, r.getSmallFont());
        }

        r.drawTextCentered("v0.0.3", w / 2, h - 20, 0.4f, 0.4f, 0.4f, 1, r.getSmallFont());
    }
}
