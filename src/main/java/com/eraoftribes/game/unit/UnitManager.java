package com.eraoftribes.game.unit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UnitManager {
    private final String dataPath;
    private final List<Unit> units = new ArrayList<>();

    public UnitManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void loadAll() {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/units.json");
            var wrapper = gson.fromJson(reader, UnitWrapper.class);
            if (wrapper != null && wrapper.units != null) {
                for (var u : wrapper.units) {
                    String id = u.get("id").getAsString();
                    String name = u.get("name").getAsString();
                    String type = u.get("type").getAsString();
                    var cost = u.containsKey("cost") ? gson.fromJson(u.get("cost"), Map.class) : Map.of();
                    int hp = u.get("hp").getAsInt();
                    int atk = u.get("attack").getAsInt();
                    int def = u.get("defense").getAsInt();
                    int move = u.get("move").getAsInt();
                    int range = u.get("range").getAsInt();
                    String special = u.containsKey("special") && !u.get("special").isJsonNull()
                        ? u.get("special").getAsString() : null;
                    units.add(new Unit(id, name, type, cost, hp, atk, def, move, range, special));
                }
            }
            reader.close();
            System.out.println("[UnitManager] Loaded " + units.size() + " units.");
        } catch (IOException e) {
            System.err.println("[UnitManager] Failed to load: " + e.getMessage());
        }
    }

    public Unit get(String id) {
        return units.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    public List<Unit> getAll() { return units; }

    private static class UnitWrapper {
        public List<Map<String, com.google.gson.JsonElement>> units;
    }
}
