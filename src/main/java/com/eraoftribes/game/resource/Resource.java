package com.eraoftribes.game.resource;

import java.util.Map;

public class Resource {
    private final String id;
    public final String name;
    public final String type;
    public final String icon;
    public final int weight;
    public final int basePrice;

    public Resource(String id, String name, String type, String icon, int weight, int basePrice) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.weight = weight;
        this.basePrice = basePrice;
    }

    public String getId() { return id; }
}
