package com.eraoftribes.game.tech;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TechManager {
    private final String dataPath;
    private final List<Technology> technologies = new ArrayList<>();

    public TechManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void loadAll() {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/technologies.json");
            var wrapper = gson.fromJson(reader, TechWrapper.class);
            if (wrapper != null && wrapper.technologies != null) {
                for (var t : wrapper.technologies) {
                    String id = t.get("id").getAsString();
                    String name = t.get("name").getAsString();
                    String era = t.get("era").getAsString();
                    int cost = t.get("cost").getAsInt();
                    var reqs = t.containsKey("requires") && !t.get("requires").isJsonNull()
                        ? gson.fromJson(t.get("requires"), List.class) : List.of();
                    var effects = t.containsKey("effects") ? gson.fromJson(t.get("effects"), Map.class) : Map.of();
                    technologies.add(new Technology(id, name, era, cost, reqs, effects));
                }
            }
            reader.close();
            System.out.println("[TechManager] Loaded " + technologies.size() + " technologies.");
        } catch (IOException e) {
            System.err.println("[TechManager] Failed to load: " + e.getMessage());
        }
    }

    public Technology get(String id) {
        return technologies.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Technology> getAll() { return technologies; }

    public List<Technology> getAvailable(List<String> researched) {
        return technologies.stream()
            .filter(t -> !researched.contains(t.getId()) && t.canResearch(researched))
            .toList();
    }

    private static class TechWrapper {
        public List<Map<String, com.google.gson.JsonElement>> technologies;
    }
}
