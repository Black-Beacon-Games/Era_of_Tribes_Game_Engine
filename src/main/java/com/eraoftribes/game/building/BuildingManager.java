package com.eraoftribes.game.building;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildingManager {
    private final String dataPath;
    private final List<Building> buildings = new ArrayList<>();

    public BuildingManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void loadAll() {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/buildings.json");
            var wrapper = gson.fromJson(reader, BuildingWrapper.class);
            if (wrapper != null && wrapper.buildings != null) {
                for (var b : wrapper.buildings) {
                    String id = b.get("id").getAsString();
                    String name = b.get("name").getAsString();
                    var cost = b.containsKey("cost") && !b.get("cost").isJsonNull()
                        ? gson.fromJson(b.get("cost"), Map.class) : Map.of();
                    int turns = b.containsKey("turns") ? b.get("turns").getAsInt() : 1;
                    var effects = b.containsKey("effects") ? gson.fromJson(b.get("effects"), Map.class) : Map.of();
                    buildings.add(new Building(id, name, cost, turns, effects));
                }
            }
            reader.close();
            System.out.println("[BuildingManager] Loaded " + buildings.size() + " buildings.");
        } catch (IOException e) {
            System.err.println("[BuildingManager] Failed to load: " + e.getMessage());
        }
    }

    public Building get(String id) {
        return buildings.stream().filter(b -> b.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Building> getAll() { return buildings; }

    private static class BuildingWrapper {
        public List<Map<String, com.google.gson.JsonElement>> buildings;
    }
}
