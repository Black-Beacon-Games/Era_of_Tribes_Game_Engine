package com.eraoftribes.game.event;

import java.util.Map;

public class GameEvent {
    private final String id;
    public final String name;
    public final String type;
    public final String trigger;
    public final double frequency;
    public final Map<String, Number> effects;
    public final String description;

    public GameEvent(String id, String name, String type, String trigger,
                     double frequency, Map<String, Number> effects, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.trigger = trigger;
        this.frequency = frequency;
        this.effects = effects;
        this.description = description;
    }

    public String getId() { return id; }
}
