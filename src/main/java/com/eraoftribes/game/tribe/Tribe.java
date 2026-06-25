package com.eraoftribes.game.tribe;

import java.util.List;
import java.util.Map;

public class Tribe {
    private final String id;
    public final String name;
    public final String description;
    public final String color;
    public final Map<String, Integer> bonus;
    public final String startingRegion;
    public final List<String> units;
    public final String specialBuilding;
    public final String leader;

    public Tribe(String id, String name, String description, String color,
                 Map<String, Integer> bonus, String startingRegion,
                 List<String> units, String specialBuilding, String leader) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.color = color;
        this.bonus = bonus;
        this.startingRegion = startingRegion;
        this.units = units;
        this.specialBuilding = specialBuilding;
        this.leader = leader;
    }

    public String getId() { return id; }

    public int getBonus(String key) {
        return bonus != null ? bonus.getOrDefault(key, 0) : 0;
    }
}
