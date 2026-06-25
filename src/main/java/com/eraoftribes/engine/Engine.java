package com.eraoftribes.engine;

import com.eraoftribes.engine.asset.AssetManager;
import com.eraoftribes.engine.audio.AudioEngine;
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

        System.out.println("[Engine] All systems initialized.");
    }

    public void start(Game game) {
        renderer.createWindow("Era of Tribes", config.renderer.resolution.width, config.renderer.resolution.height);
        sceneManager.switchTo("loading");
        gameLoop(game);
    }

    private void gameLoop(Game game) {
        while (!renderer.shouldClose()) {
            input.poll();
            game.update(0.016);
            sceneManager.update(0.016);
            renderer.beginFrame();
            sceneManager.render(renderer);
            renderer.endFrame();
        }
        shutdown();
    }

    private void shutdown() {
        saveManager.saveAll();
        network.disconnect();
        audio.stopAll();
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
    public String getGamePath() { return gamePath; }
}
