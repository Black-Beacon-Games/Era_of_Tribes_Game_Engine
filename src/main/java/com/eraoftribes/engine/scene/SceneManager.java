package com.eraoftribes.engine.scene;

import com.eraoftribes.engine.rendering.Renderer;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private final Map<String, Scene> scenes = new HashMap<>();
    private Scene currentScene;
    private String currentId;

    public void register(String id, Scene scene) {
        scenes.put(id, scene);
    }

    public void switchTo(String id) {
        if (currentScene != null) currentScene.onLeave();
        currentScene = scenes.get(id);
        currentId = id;
        if (currentScene != null) currentScene.onEnter();
        System.out.println("[SceneManager] Switched to: " + id);
    }

    public void update(double dt) {
        if (currentScene != null) currentScene.update(dt);
    }

    public void render(Renderer renderer) {
        if (currentScene != null) currentScene.render(renderer);
    }

    public String getCurrentSceneId() {
        return currentId;
    }
}
