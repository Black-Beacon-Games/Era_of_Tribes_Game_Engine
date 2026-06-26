package com.eraoftribes.engine;

import com.eraoftribes.engine.asset.AssetManager;
import com.eraoftribes.engine.audio.AudioEngine;
import com.eraoftribes.engine.discord.DiscordConfig;
import com.eraoftribes.engine.discord.DiscordManager;
import com.eraoftribes.engine.input.InputManager;
import com.eraoftribes.engine.networking.NetworkManager;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.save.SaveManager;
import com.eraoftribes.engine.scene.SceneManager;
import com.eraoftribes.engine.script.ScriptEngine;
import com.eraoftribes.engine.ui.UIEngine;
import com.eraoftribes.engine.world.WorldGenerator;
import com.eraoftribes.game.Game;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class Engine {
    private final String[] args;
    private EngineConfig config;
    private ModuleManager moduleManager;
    private AssetManager assetManager;
    private Renderer renderer;
    private AudioEngine audio;
    private InputManager input;
    private NetworkManager network;
    private SaveManager saveManager;
    private SceneManager sceneManager;
    private UIEngine ui;
    private ScriptEngine scriptEngine;
    private WorldGenerator worldGenerator;
    private DiscordManager discord;
    private String lastSceneId;
    private String gamePath;
    private Game game;

    private DebugConsole debugConsole;
    private boolean lastConsoleState;

    private long lastFpsTime;
    private int frameCount;
    private int fps;
    private double frameTimeMs;

    public Engine(String[] args) {
        this.args = args;
    }

    public void init() {
        System.out.println("============================================");
        System.out.println(" Era of Tribes Engine v0.0.3");
        System.out.println("============================================");

        parseArgs();

        try {
            config = EngineConfig.load(gamePath + "/../engine/config/engine.json");
            System.out.println("[Engine] Config loaded.");
        } catch (IOException e) {
            System.err.println("[Engine] Failed to load config: " + e.getMessage());
            config = new EngineConfig();
        }

        moduleManager = new ModuleManager();
        try {
            moduleManager.loadConfig(gamePath + "/../engine/config/modules.json");
        } catch (IOException e) {
            System.err.println("[Engine] Failed to load module config: " + e.getMessage());
        }
        moduleManager.initialize();

        assetManager = new AssetManager(gamePath + "/assets");
        renderer = new Renderer(config.renderer);
        audio = new AudioEngine(config.audio);
        input = new InputManager(config.input);
        network = new NetworkManager(config.multiplayer);
        saveManager = new SaveManager(gamePath + "/saves");
        sceneManager = new SceneManager();
        ui = new UIEngine(gamePath + "/ui");
        scriptEngine = new ScriptEngine();
        worldGenerator = new WorldGenerator();
        debugConsole = new DebugConsole();

        DiscordConfig dc = new DiscordConfig();
        try {
            dc = new com.google.gson.Gson().fromJson(
                new java.io.FileReader(gamePath + "/config/discord.json"), DiscordConfig.class);
        } catch (Exception e) {
            System.out.println("[Engine] No discord.json found, using defaults.");
        }
        discord = new DiscordManager(dc);

        System.out.println("[Engine] All systems initialized.");
    }

    public void start(Game game) {
        this.game = game;
        renderer.createWindow("Era of Tribes", config.renderer.resolution.width, config.renderer.resolution.height);
        new Thread(() -> { try { discord.init(); } catch (Exception e) { System.err.println("[Discord] Init error: " + e.getMessage()); } }, "discord-init").start();
        try {
            sceneManager.switchTo("loading");
        } catch (Exception e) {
            System.err.println("[Engine] Scene switch error: " + e.getMessage());
            e.printStackTrace();
        }
        gameLoop();
    }

    private void gameLoop() {
        long lastTime = System.nanoTime();
        lastFpsTime = System.currentTimeMillis();
        int targetFps = config.renderer != null && config.renderer.targetFps > 0
            ? config.renderer.targetFps : 200;
        double targetInterval = 1_000_000_000.0 / targetFps;

        while (!renderer.shouldClose()) {
            try {
                long now = System.nanoTime();
                double delta = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                if (delta > 0.1) delta = 0.016;
                frameTimeMs = delta * 1000;

                frameCount++;
                long nowMs = System.currentTimeMillis();
                if (nowMs - lastFpsTime >= 1000) {
                    fps = frameCount;
                    frameCount = 0;
                    lastFpsTime = nowMs;
                }

                if (config.debug != null && config.debug.console != lastConsoleState) {
                    lastConsoleState = config.debug.console;
                    if (config.debug.console) debugConsole.show();
                    else debugConsole.hide();
                }

                input.poll();
                game.update(delta);
                sceneManager.update(delta);

                String sid = sceneManager.getCurrentSceneId();
                if (sid != null && !sid.equals(lastSceneId)) {
                    lastSceneId = sid;
                    updateDiscordPresence(sid);
                }

                renderer.beginFrame();
                sceneManager.render(renderer);
                renderDebugOverlay(renderer);
                renderer.endFrame();

                long elapsed = System.nanoTime() - now;
                if (elapsed < targetInterval) {
                    long sleepNs = (long) (targetInterval - elapsed);
                    Thread.sleep(sleepNs / 1_000_000, (int) (sleepNs % 1_000_000));
                }
            } catch (Exception e) {
                System.err.println("[Engine] Game loop error: " + e.getMessage());
                System.out.println("[Engine] Game loop error: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        shutdown();
    }

    private void renderDebugOverlay(Renderer r) {
        EngineConfig.DebugConfig debug = config.debug;
        if (debug == null) return;
        boolean any = debug.fpsOverlay || debug.profiling || debug.showMemory
            || debug.showVersion || debug.showCoordinates;
        if (!any) return;

        int x = 10;
        int y = 10;
        int lineH = 18;
        var font = r.getSmallFont();

        if (debug.fpsOverlay) {
            r.drawText("FPS: " + fps, x, y += lineH, 0, 1, 0, 1, font);
        }
        if (debug.profiling) {
            r.drawText(String.format("Frame: %.2f ms", frameTimeMs), x, y += lineH, 1, 1, 0, 1, font);
            Runtime rt = Runtime.getRuntime();
            long used = rt.totalMemory() - rt.freeMemory();
            r.drawText("Heap: " + (used / 1024 / 1024) + " MB", x, y += lineH, 1, 1, 0, 1, font);
        }
        if (debug.showMemory) {
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = mem.getHeapMemoryUsage();
            MemoryUsage nonHeap = mem.getNonHeapMemoryUsage();
            r.drawText("Heap: " + (heap.getUsed() / 1024 / 1024) + "/" + (heap.getMax() / 1024 / 1024) + " MB", x, y += lineH, 0, 1, 1, 1, font);
            r.drawText("Non-Heap: " + (nonHeap.getUsed() / 1024 / 1024) + "/" + (nonHeap.getMax() / 1024 / 1024) + " MB", x, y += lineH, 0, 1, 1, 1, font);
            r.drawText("Threads: " + ManagementFactory.getThreadMXBean().getThreadCount(), x, y += lineH, 0, 1, 1, 1, font);
        }
        if (debug.showVersion) {
            int rx = r.getWidth() - 200;
            int ry = 10;
            r.drawText("Engine: " + (config.version != null ? config.version : "?"), rx, ry += lineH, 0.5f, 0.8f, 1, 1, font);
            r.drawText("Game: " + "Era of Tribes", rx, ry += lineH, 0.5f, 0.8f, 1, 1, font);
            r.drawText("Java: " + System.getProperty("java.version"), rx, ry += lineH, 0.5f, 0.8f, 1, 1, font);
            r.drawText("OS: " + System.getProperty("os.name"), rx, ry += lineH, 0.5f, 0.8f, 1, 1, font);
        }
        if (debug.showCoordinates) {
            int mx = r.getMouseX();
            int my = r.getMouseY();
            int cx = r.getWidth() - 200;
            int cy = r.getHeight() - 40;
            r.drawText("Mouse: " + mx + ", " + my, cx, cy, 1, 0.5f, 0.5f, 1, font);
        }
    }

    private void updateDiscordPresence(String sceneId) {
        String details = "Playing Era of Tribes";
        String state = switch (sceneId) {
            case "loading" -> "Loading...";
            case "main_menu" -> "In main menu";
            case "settings" -> "In settings";
            case "game" -> "In game";
            case "lobby" -> "In lobby";
            default -> "In " + sceneId;
        };
        String finalState = state;
        new Thread(() -> {
            try {
                discord.updatePresence(finalState, details);
            } catch (Exception e) {
                System.err.println("[Discord] Presence update error: " + e.getMessage());
            }
        }, "discord-presence").start();
    }

    private void shutdown() {
        config.save();
        saveManager.saveAll();
        network.disconnect();
        audio.stopAll();
        discord.shutdown();
        renderer.destroy();
        System.out.println("[Engine] Shutdown complete.");
    }

    private void parseArgs() {
        for (int i = 0; i < args.length; i++) {
            if ("--game".equals(args[i]) && i + 1 < args.length) {
                gamePath = new File(args[i + 1]).getAbsolutePath();
            }
        }
        if (gamePath == null) {
            gamePath = new File("game").getAbsolutePath();
        }
        System.out.println("[Engine] Game path: " + gamePath);
    }

    public EngineConfig getConfig() { return config; }
    public AssetManager getAssetManager() { return assetManager; }
    public Renderer getRenderer() { return renderer; }
    public AudioEngine getAudio() { return audio; }
    public InputManager getInput() { return input; }
    public NetworkManager getNetwork() { return network; }
    public SaveManager getSaveManager() { return saveManager; }
    public SceneManager getSceneManager() { return sceneManager; }
    public UIEngine getUI() { return ui; }
    public ScriptEngine getScriptEngine() { return scriptEngine; }
    public WorldGenerator getWorldGenerator() { return worldGenerator; }
    public DiscordManager getDiscord() { return discord; }
    public String getGamePath() { return gamePath; }
    public Game getGame() { return game; }
}
