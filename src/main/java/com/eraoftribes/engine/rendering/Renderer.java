package com.eraoftribes.engine.rendering;

import com.eraoftribes.engine.EngineConfig.RendererConfig;

public class Renderer {
    private final RendererConfig config;
    private boolean running;

    public Renderer(RendererConfig config) {
        this.config = config;
        System.out.println("[Renderer] Initialized (backend=" + config.backend + ", vsync=" + config.vsync + ")");
    }

    public void createWindow(String title, int width, int height) {
        System.out.println("[Renderer] Creating window: " + title + " (" + width + "x" + height + ")");
        running = true;
    }

    public void beginFrame() {}
    public void endFrame() {}

    public boolean shouldClose() { return !running; }

    public void destroy() {
        running = false;
        System.out.println("[Renderer] Shutdown.");
    }

    public void clear(float r, float g, float b, float a) {}
    public void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {}
    public void drawTexture(int textureId, float x, float y, float w, float h) {}
    public int loadTexture(String path) { return 0; }
}
