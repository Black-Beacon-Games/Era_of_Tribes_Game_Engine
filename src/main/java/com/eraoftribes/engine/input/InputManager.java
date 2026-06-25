package com.eraoftribes.engine.input;

import com.eraoftribes.engine.EngineConfig.InputConfig;

public class InputManager {
    private final InputConfig config;
    private double mouseX, mouseY;
    private boolean[] keys = new boolean[512];
    private boolean[] buttons = new boolean[8];

    public InputManager(InputConfig config) {
        this.config = config;
        System.out.println("[InputManager] Initialized.");
    }

    public void poll() {}

    public boolean isKeyDown(int key) { return keys[key]; }
    public boolean isMouseButtonDown(int btn) { return buttons[btn]; }
    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }

    public void setMousePos(double x, double y) { mouseX = x; mouseY = y; }
    public void setKey(int key, boolean pressed) { keys[key] = pressed; }
    public void setMouseButton(int btn, boolean pressed) { buttons[btn] = pressed; }
}
