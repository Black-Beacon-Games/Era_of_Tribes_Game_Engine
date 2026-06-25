package com.eraoftribes.engine.ui;

import java.util.ArrayList;
import java.util.List;

public class UIScreen {
    private final String id;
    private final List<UIComponent> components = new ArrayList<>();
    private String backgroundImage;

    public UIScreen(String id) {
        this.id = id;
    }

    public void addComponent(UIComponent component) {
        components.add(component);
    }

    public void setBackground(String image) {
        this.backgroundImage = image;
    }

    public void render() {
        for (var c : components) {
            if (c.isVisible()) c.render();
        }
    }

    public void update(double dt) {
        for (var c : components) {
            if (c.isVisible()) c.update(dt);
        }
    }

    public String getId() { return id; }
}
