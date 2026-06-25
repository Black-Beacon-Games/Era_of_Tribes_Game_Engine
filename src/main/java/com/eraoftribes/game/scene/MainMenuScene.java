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

    public MainMenuScene(Engine engine, Game game) {
        super("main_menu");
        this.engine = engine;
        this.game = game;
        this.selected = 0;
    }

    public void onEnter() {
        selected = 0;
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

        r.drawRect(0, 0, w, h, 0.05f, 0.05f, 0.08f, 1);

        r.drawTextCentered("ERA OF TRIBES", cx, (int) (h * 0.2), 0.8f, 0.6f, 0.2f, 1, r.getTitleFont());
        r.drawTextCentered("v1.0.0", cx, (int) (h * 0.2) + 35, 0.5f, 0.5f, 0.5f, 1, r.getSmallFont());

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
            case 3 -> System.exit(0);
        }
    }
}
