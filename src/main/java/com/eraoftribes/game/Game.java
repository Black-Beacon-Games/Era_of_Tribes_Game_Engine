package com.eraoftribes.game;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.scene.Scene;
import com.eraoftribes.game.combat.CombatSystem;
import com.eraoftribes.game.diplomacy.DiplomacySystem;
import com.eraoftribes.game.map.MapManager;
import com.eraoftribes.game.tribe.TribeManager;
import com.eraoftribes.game.tech.TechManager;
import com.eraoftribes.game.building.BuildingManager;
import com.eraoftribes.game.unit.UnitManager;
import com.eraoftribes.game.resource.ResourceManager;
import com.eraoftribes.game.event.EventManager;
import com.eraoftribes.game.scene.CreditsScene;
import com.eraoftribes.game.scene.LoadingScene;
import com.eraoftribes.game.scene.MainMenuScene;
import com.eraoftribes.game.scene.SettingsScene;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Engine engine;
    private int turn;
    private int maxTurns;
    private String phase;
    private final List<GamePlayer> players = new ArrayList<>();
    private int currentPlayerIndex;

    private TribeManager tribeManager;
    private TechManager techManager;
    private BuildingManager buildingManager;
    private UnitManager unitManager;
    private ResourceManager resourceManager;
    private EventManager eventManager;
    private MapManager mapManager;
    private DiplomacySystem diplomacySystem;
    private CombatSystem combatSystem;

    private final String[] phases = {"start", "taxes", "production", "research", "trade", "movement", "combat", "events", "end"};

    public Game(Engine engine) {
        this.engine = engine;
    }

    public void init() {
        tribeManager = new TribeManager(engine.getGamePath() + "/tribes");
        techManager = new TechManager(engine.getGamePath() + "/technologies");
        buildingManager = new BuildingManager(engine.getGamePath() + "/buildings");
        unitManager = new UnitManager(engine.getGamePath() + "/units");
        resourceManager = new ResourceManager(engine.getGamePath() + "/resources");
        eventManager = new EventManager(engine.getGamePath() + "/events");
        mapManager = new MapManager(engine.getGamePath() + "/maps");
        diplomacySystem = new DiplomacySystem();
        combatSystem = new CombatSystem();

        turn = 0;
        maxTurns = 200;
        phase = "start";

        registerScenes();
        System.out.println("[Game] Initialized.");
    }

    private void registerScenes() {
        engine.getSceneManager().register("loading", new LoadingScene(engine, this));
        engine.getSceneManager().register("main_menu", new MainMenuScene(engine, this));
        engine.getSceneManager().register("settings", new SettingsScene(engine));
        engine.getSceneManager().register("credits", new CreditsScene(engine));
        engine.getSceneManager().register("game", new Scene("game") {
            public void onEnter() { startGame(); }
        });
        engine.getSceneManager().register("lobby", new Scene("lobby") {});
        engine.getSceneManager().register("map_editor", new Scene("map_editor") {});
    }

    public void startGame() {
        turn = 0;
        phase = "start";
        currentPlayerIndex = 0;
        System.out.println("[Game] New game started!");
        startTurn();
    }

    public void startTurn() {
        turn++;
        phase = "start";
        System.out.println("=== Turn " + turn + " ===");
        nextPhase();
    }

    public void nextPhase() {
        for (int i = 0; i < phases.length - 1; i++) {
            if (phases[i].equals(phase)) {
                phase = phases[i + 1];
                break;
            }
        }
        executePhase();
    }

    private void executePhase() {
        System.out.println("[Game] Phase: " + phase);
        switch (phase) {
            case "start" -> { nextPhase(); }
            case "taxes" -> { doTaxes(); nextPhase(); }
            case "production" -> { doProduction(); nextPhase(); }
            case "research" -> { doResearch(); nextPhase(); }
            case "trade" -> {} // player action
            case "movement" -> {} // player action
            case "combat" -> { doCombat(); nextPhase(); }
            case "events" -> { doEvents(); nextPhase(); }
            case "end" -> { doEnd(); }
        }
    }

    private void doTaxes() {
        for (var p : players) {
            int tax = (int) (p.population * 0.5 * p.taxRate);
            p.gold += tax;
        }
    }

    private void doProduction() {
        for (var p : players) {
            for (var b : p.buildings) {
                b.produce(p);
            }
        }
    }

    private void doResearch() {}
    private void doCombat() {}
    private void doEvents() {}

    private void doEnd() {
        if (turn < maxTurns) startTurn();
    }

    public void update(double dt) {}

    public int getTurn() { return turn; }
    public String getPhase() { return phase; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public GamePlayer getCurrentPlayer() { return players.isEmpty() ? null : players.get(currentPlayerIndex); }
    public List<GamePlayer> getPlayers() { return players; }

    public TribeManager getTribeManager() { return tribeManager; }
    public TechManager getTechManager() { return techManager; }
    public BuildingManager getBuildingManager() { return buildingManager; }
    public UnitManager getUnitManager() { return unitManager; }
    public ResourceManager getResourceManager() { return resourceManager; }
    public EventManager getEventManager() { return eventManager; }
    public MapManager getMapManager() { return mapManager; }
    public DiplomacySystem getDiplomacySystem() { return diplomacySystem; }
    public CombatSystem getCombatSystem() { return combatSystem; }
}
