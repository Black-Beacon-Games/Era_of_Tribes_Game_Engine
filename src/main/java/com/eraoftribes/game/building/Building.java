package com.eraoftribes.game.building;

import com.eraoftribes.game.GamePlayer;
import java.util.Map;

public class Building {
    private final String id;
    public final String name;
    public final Map<String, Integer> cost;
    public final int buildTurns;
    public final Map<String, Object> effects;
    public int remainingTurns;

    public Building(String id, String name, Map<String, Integer> cost,
                    int buildTurns, Map<String, Object> effects) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.buildTurns = buildTurns;
        this.effects = effects;
        this.remainingTurns = buildTurns;
    }

    public String getId() { return id; }

    public void produce(GamePlayer player) {
        if (effects == null) return;
        if (effects.containsKey("food")) player.population += ((Number) effects.get("food")).intValue();
        if (effects.containsKey("gold")) player.gold += ((Number) effects.get("gold")).intValue();
    }

    public boolean canAfford(GamePlayer player) {
        if (cost == null) return true;
        return player.gold >= cost.getOrDefault("gold", 0);
    }
}
