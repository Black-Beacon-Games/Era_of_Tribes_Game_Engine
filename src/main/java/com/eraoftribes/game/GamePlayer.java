package com.eraoftribes.game;

import com.eraoftribes.game.building.Building;
import com.eraoftribes.game.tribe.Tribe;
import com.eraoftribes.game.unit.Unit;
import java.util.ArrayList;
import java.util.List;

public class GamePlayer {
    private final String id;
    public String name;
    public Tribe tribe;
    public int gold;
    public int population;
    public double taxRate = 0.3;
    public int happiness = 50;
    public int stability = 50;
    public int faith;
    public int prestige;
    public final List<Building> buildings = new ArrayList<>();
    public final List<Unit> units = new ArrayList<>();
    public final List<String> researchedTechs = new ArrayList<>();
    public final List<String> cities = new ArrayList<>();
    public boolean isAI;

    public GamePlayer(String id, String name) {
        this.id = id;
        this.name = name;
        this.gold = 100;
        this.population = 20;
    }

    public String getId() { return id; }
}
