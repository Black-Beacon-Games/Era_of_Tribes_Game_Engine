package com.eraoftribes.game.resource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {
    private final String dataPath;
    private final List<Resource> resources = new ArrayList<>();
    private final Map<String, List<String>> terrainResources = new HashMap<>();

    public ResourceManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void loadAll() {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/resources.json");
            var wrapper = gson.fromJson(reader, ResourceWrapper.class);
            if (wrapper != null && wrapper.resources != null) {
                for (var r : wrapper.resources) {
                    String id = r.get("id").getAsString();
                    String name = r.get("name").getAsString();
                    String type = r.get("type").getAsString();
                    String icon = r.get("icon").getAsString();
                    int weight = r.get("weight").getAsInt();
                    int price = r.get("basePrice").getAsInt();
                    resources.add(new Resource(id, name, type, icon, weight, price));
                }
            }
            if (wrapper != null && wrapper.terrainResources != null) {
                for (var entry : wrapper.terrainResources.entrySet()) {
                    terrainResources.put(entry.getKey(), entry.getValue());
                }
            }
            reader.close();
            System.out.println("[ResourceManager] Loaded " + resources.size() + " resources.");
        } catch (IOException e) {
            System.err.println("[ResourceManager] Failed to load: " + e.getMessage());
        }
    }

    public Resource get(String id) {
        return resources.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Resource> getAll() { return resources; }

    public List<String> getResourcesForTerrain(String terrain) {
        return terrainResources.getOrDefault(terrain, List.of());
    }

    private static class ResourceWrapper {
        public List<Map<String, com.google.gson.JsonElement>> resources;
        public Map<String, List<String>> terrainResources;
    }
}
