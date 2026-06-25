package com.eraoftribes.game.tech;

import java.util.List;
import java.util.Map;

public class Technology {
    private final String id;
    public final String name;
    public final String era;
    public final int cost;
    public final List<String> requires;
    public final Map<String, Object> effects;

    public Technology(String id, String name, String era, int cost,
                      List<String> requires, Map<String, Object> effects) {
        this.id = id;
        this.name = name;
        this.era = era;
        this.cost = cost;
        this.requires = requires;
        this.effects = effects;
    }

    public String getId() { return id; }

    public boolean canResearch(List<String> researched) {
        if (requires == null || requires.isEmpty()) return true;
        return researched.containsAll(requires);
    }
}
