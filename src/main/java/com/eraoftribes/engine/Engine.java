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

    public Engine(String[] args) {
        this.args = args;
    }

    public void init() {
        System.out.println("============================================");
        System.out.println(" Era of Tribes Engine v1.0.0");
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
        renderer.createWindow("Era of Tribes", config.renderer.resolution.width, config.renderer.resolution.height);
        new Thread(() -> { try { discord.init(); } catch (Exception e) { System.err.println("[Discord] Init error: " + e.getMessage()); } }, "discord-init").start();
        try {
            sceneManager.switchTo("loading");
        } catch (Exception e) {
            System.err.println("[Engine] Scene switch error: " + e.getMessage());
            e.printStackTrace();
        }
        gameLoop(game);
    }

    private void gameLoop(Game game) {
        long lastTime = 0;
        double nsPerTick = 1000000000.0 / 60.0;
        while (!renderer.shouldClose()) {
            try {
                long now = System.nanoTime();
                if (now - lastTime < nsPerTick) {
                    try { Thread.sleep(1); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                    continue;
                }
                lastTime = now;

                input.poll();
                game.update(0.016);
                sceneManager.update(0.016);

                String sid = sceneManager.getCurrentSceneId();
                if (sid != null && !sid.equals(lastSceneId)) {
                    lastSceneId = sid;
                    updateDiscordPresence(sid);
                }

                renderer.beginFrame();
                sceneManager.render(renderer);
                renderer.endFrame();
            } catch (Exception e) {
                System.err.println("[Engine] Game loop error: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
        shutdown();
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
}
