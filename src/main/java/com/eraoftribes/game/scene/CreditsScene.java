package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;

public class CreditsScene extends Scene {
    private final Engine engine;

    private static final String[] CREDITS = {
        "Game Design by Arne Lorenz",
        "Software Engineering & Web Development by Kilian Bogus"
    };

    public CreditsScene(Engine engine) {
        super("credits");
        this.engine = engine;
    }

    public void render(Renderer r) {
        int w = r.getWidth();
        int h = r.getHeight();
        int cx = w / 2;

        r.drawRect(0, 0, w, h, 0.05f, 0.05f, 0.08f, 1);
        r.drawTextCentered("CREDITS", cx, 45, 0.8f, 0.6f, 0.2f, 1, r.getHeaderFont());

        int startY = h / 2 - (CREDITS.length * 40) / 2;
        for (int i = 0; i < CREDITS.length; i++) {
            r.drawTextCentered(CREDITS[i], cx, startY + i * 40, 0.9f, 0.9f, 0.9f, 1, r.getBodyFont());
        }

        int by = startY + CREDITS.length * 40 + 40;
        if (r.drawButton("Back", cx - 110, by, 220, 45)) {
            engine.getSceneManager().switchTo("main_menu");
        }
    }
}
