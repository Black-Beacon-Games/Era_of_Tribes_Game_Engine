package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;
import com.eraoftribes.game.Game;

public class MainMenuScene extends Scene {
    private final Engine engine;
    private final Game game;

    private static final String[] LABELS = {"Play", "Settings", "Credits", "Quit"};
    private int selected;

    private int bgTex;
    private int logoTex;
    private boolean assetsReady;

    public MainMenuScene(Engine engine, Game game) {
        super("main_menu");
        this.engine = engine;
        this.game = game;
        this.selected = 0;
        this.bgTex = 0;
        this.logoTex = 0;
        this.assetsReady = false;
    }

    public void onEnter() {
        selected = 0;
        assetsReady = false;
        loadAssets();
        String base = engine.getGamePath() + "/audio/";
        engine.getAudio().loadTrack("menu_music", base + "menu.mp3", true, 0.7);
        engine.getAudio().play("menu_music");
    }

    public void onLeave() {
        engine.getAudio().stop("menu_music");
    }

    private void loadAssets() {
        var r = engine.getRenderer();
        String base = "assets/menu/";

        bgTex = r.loadTexture(base + "background.png");
        if (bgTex == 0) bgTex = r.loadTexture(base + "background.jpg");
        if (bgTex == 0) bgTex = r.loadTexture(base + "background.jpeg");

        logoTex = r.loadTexture(base + "logo.svg",
            r.getWidth() > 0 ? r.getWidth() / 2 : 400, 120);

        assetsReady = bgTex != 0;
        System.out.println("[MainMenu] Assets loaded: bg=" + (bgTex != 0)
            + " logo=" + (logoTex != 0));
    }

    public void update(double dt) {
        int key = engine.getRenderer().getLastKey();
        if (key == 38 || key == 87) { selected = (selected - 1 + LABELS.length) % LABELS.length; }
        if (key == 40 || key == 83) { selected = (selected + 1) % LABELS.length; }
        if (key == 10) { activate(); }
    }

    public void render(Renderer r) {
        int w = r.getWidth();
        int h = r.getHeight();
        int cx = w / 2;

        if (assetsReady) {
            r.drawTextureCover(bgTex);
            r.drawRect(0, 0, w, h, 0, 0, 0, 0.35f);
        } else {
            r.drawRect(0, 0, w, h, 0.05f, 0.05f, 0.08f, 1);
        }

        int logoW = (int) (w * 0.5f);
        int logoH = (int) (logoW * 0.3f);
        int logoX = (w - logoW) / 2;
        int logoY = (int) (h * 0.12);

        if (logoTex != 0) {
            r.drawTexture(logoTex, logoX, logoY, logoW, logoH);
        } else {
            r.drawTextCentered("ERA OF TRIBES", cx, (int) (h * 0.2), 0.8f, 0.6f, 0.2f, 1, r.getTitleFont());
        }
        r.drawTextCentered("v0.0.3", cx, logoY + logoH + 15, 0.5f, 0.5f, 0.5f, 1, r.getSmallFont());

        int btnW = 220;
        int btnH = 50;
        int startY = (int) (h * 0.4);
        int gap = 12;

        for (int i = 0; i < LABELS.length; i++) {
            int by = startY + i * (btnH + gap);
            boolean hover = r.getMouseX() >= cx - btnW / 2 && r.getMouseX() <= cx + btnW / 2
                && r.getMouseY() >= by && r.getMouseY() <= by + btnH;

            if (hover) r.drawRect(cx - btnW / 2, by, btnW, btnH, 0.3f, 0.5f, 0.8f, 1);
            else if (i == selected) r.drawRect(cx - btnW / 2, by, btnW, btnH, 0.2f, 0.35f, 0.6f, 1);
            else r.drawRect(cx - btnW / 2, by, btnW, btnH, 0.12f, 0.12f, 0.15f, 1);

            r.drawRect(cx - btnW / 2, by, btnW, btnH, 0.3f, 0.3f, 0.35f, 0.3f);
            r.drawTextCentered(LABELS[i], cx, by + btnH / 2, 1, 1, 1, 1);

            if (hover && r.isMouseClicked()) {
                r.consumeClick();
                selected = i;
                activate();
                return;
            }
        }
    }

    private void activate() {
        switch (selected) {
            case 0 -> engine.getSceneManager().switchTo("game");
            case 1 -> engine.getSceneManager().switchTo("settings");
            case 2 -> engine.getSceneManager().switchTo("credits");
            case 3 -> engine.requestShutdown();
        }
    }
}
