package com.eraoftribes.engine.scene;

import com.eraoftribes.engine.rendering.Renderer;

public abstract class Scene {
    protected final String id;
    protected String nextScene;

    public Scene(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    public void onEnter() {}
    public void onLeave() {}
    public void update(double dt) {}
    public void render(Renderer renderer) {}
}
