package com.eraoftribes.game.map;

import com.google.gson.Gson;
import com.eraoftribes.engine.world.WorldGenerator;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class MapManager {
    private final String dataPath;
    private WorldConfig currentConfig;
    private WorldGenerator.WorldData worldData;

    public MapManager(String dataPath) {
        this.dataPath = dataPath;
    }

    public void load(String name) {
        try {
            Gson gson = new Gson();
            var reader = new FileReader(dataPath + "/" + name + ".json");
            currentConfig = gson.fromJson(reader, WorldConfig.class);
            reader.close();
            System.out.println("[MapManager] Loaded map: " + name);
        } catch (IOException e) {
            System.err.println("[MapManager] Failed to load map: " + e.getMessage());
        }
    }

    public WorldGenerator.WorldData generate(long seed) {
        var gen = new WorldGenerator();
        worldData = gen.generate(seed, 1920, 1080);
        return worldData;
    }

    public WorldConfig getConfig() { return currentConfig; }
    public WorldGenerator.WorldData getWorldData() { return worldData; }

    public static class WorldConfig {
        public String name;
        public String version;
        public SizeConfig size;
        public int hexSize;
        public String hexShape;
        public long seed;
        public Map<String, RegionConfig> regions;
    }

    public static class SizeConfig {
        public int width;
        public int height;
    }

    public static class RegionConfig {
        public String name;
        public String climate;
        public double fertility;
    }
}
