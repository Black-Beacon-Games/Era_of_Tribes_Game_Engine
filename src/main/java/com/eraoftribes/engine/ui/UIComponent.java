package com.eraoftribes.engine.ui;

public class UIComponent {
    private final String id;
    private float x, y, width, height;
    private Runnable clickAction;
    private boolean visible;

    public UIComponent(String id, float x, float y, float width, float height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visible = true;
    }

    public void onClick(Runnable action) { this.clickAction = action; }
    public void triggerClick() { if (clickAction != null) clickAction.run(); }
    public void setVisible(boolean v) { visible = v; }
    public boolean isVisible() { return visible; }

    public String getId() { return id; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void render() {}
    public void update(double dt) {}
}
