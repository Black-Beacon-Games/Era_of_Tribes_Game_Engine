package com.eraoftribes.game.tribe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TribeManager {
    private final String dataPath;
    private final List<Tribe> tribes = new ArrayList<>();

    public TribeManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void loadAll() {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/tribes.json");
            var wrapper = gson.fromJson(reader, TribesWrapper.class);
            if (wrapper != null && wrapper.tribes != null) {
                for (var t : wrapper.tribes) {
                    String id = t.get("id").getAsString();
                    String name = t.get("name").getAsString();
                    String desc = t.get("description").getAsString();
                    String color = t.get("color").getAsString();
                    var bonus = gson.fromJson(t.get("bonus"), Map.class);
                    String region = t.get("startingRegion").getAsString();
                    var units = gson.fromJson(t.get("units"), List.class);
                    String building = t.get("specialBuilding").getAsString();
                    String leader = t.get("leader").getAsString();
                    tribes.add(new Tribe(id, name, desc, color, bonus, region, units, building, leader));
                }
            }
            reader.close();
            System.out.println("[TribeManager] Loaded " + tribes.size() + " tribes.");
        } catch (IOException e) {
            System.err.println("[TribeManager] Failed to load: " + e.getMessage());
        }
    }

    public Tribe get(String id) {
        return tribes.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Tribe> getAll() { return tribes; }

    private static class TribesWrapper {
        public List<Map<String, com.google.gson.JsonElement>> tribes;
    }
}
