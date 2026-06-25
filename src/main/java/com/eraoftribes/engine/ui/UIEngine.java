package com.eraoftribes.engine.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UIEngine {
    private final String uiPath;
    private final Map<String, UIComponent> components = new HashMap<>();
    private UIScreen currentScreen;

    public UIEngine(String uiPath) {
        this.uiPath = uiPath;
        System.out.println("[UIEngine] Path: " + uiPath);
    }

    public void registerComponent(String id, UIComponent component) {
        components.put(id, component);
    }

    public void showScreen(UIScreen screen) {
        currentScreen = screen;
    }

    public void onClick(String componentId, Runnable action) {
        var component = components.get(componentId);
        if (component != null) {
            component.onClick(action);
        }
    }

    public void render() {
        if (currentScreen != null) {
            currentScreen.render();
        }
    }

    public void update(double dt) {
        if (currentScreen != null) {
            currentScreen.update(dt);
        }
    }
}
