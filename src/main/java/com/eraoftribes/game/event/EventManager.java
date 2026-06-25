package com.eraoftribes.game.event;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EventManager {
    private final String dataPath;
    private final List<GameEvent> events = new ArrayList<>();
    private final Random random = new Random();

    public EventManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void loadAll() {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/events.json");
            var wrapper = gson.fromJson(reader, EventWrapper.class);
            if (wrapper != null && wrapper.events != null) {
                for (var e : wrapper.events) {
                    String id = e.get("id").getAsString();
                    String name = e.get("name").getAsString();
                    String type = e.get("type").getAsString();
                    String trigger = e.get("trigger").getAsString();
                    double freq = e.containsKey("frequency") ? e.get("frequency").getAsDouble() : 0.01;
                    var effects = e.containsKey("effects") ? gson.fromJson(e.get("effects"), Map.class) : Map.of();
                    String desc = e.get("description").getAsString();
                    events.add(new GameEvent(id, name, type, trigger, freq, effects, desc));
                }
            }
            reader.close();
            System.out.println("[EventManager] Loaded " + events.size() + " events.");
        } catch (IOException e) {
            System.err.println("[EventManager] Failed to load: " + e.getMessage());
        }
    }

    public List<GameEvent> getActiveEvents() {
        return events.stream()
            .filter(e -> random.nextDouble() < e.frequency)
            .toList();
    }

    public GameEvent get(String id) {
        return events.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    public List<GameEvent> getAll() { return events; }

    private static class EventWrapper {
        public List<Map<String, com.google.gson.JsonElement>> events;
    }
}
