package com.eraoftribes.engine.world;

import java.util.Random;

public class WorldGenerator {
    private long seed;
    private Random random;

    public WorldGenerator() {
        System.out.println("[WorldGenerator] Initialized.");
    }

    public WorldData generate(long seed, int width, int height) {
        this.seed = seed;
        this.random = new Random(seed);

        var data = new WorldData(width, height);
        generateTerrain(data);
        generateBiomes(data);
        generateRivers(data);
        generateResources(data);

        System.out.println("[WorldGenerator] World generated: " + width + "x" + height + ", seed=" + seed);
        return data;
    }

    private void generateTerrain(WorldData data) {
        for (int y = 0; y < data.height; y++) {
            for (int x = 0; x < data.width; x++) {
                data.heightMap[y][x] = perlinNoise(x * 0.01, y * 0.01, 6);
            }
        }
    }

    private void generateBiomes(WorldData data) {
        for (int y = 0; y < data.height; y++) {
            for (int x = 0; x < data.width; x++) {
                double h = data.heightMap[y][x];
                double lat = Math.abs((double) y / data.height - 0.5) * 2;
                if (h < 0.2) data.biomes[y][x] = "water";
                else if (h < 0.35) data.biomes[y][x] = lat > 0.7 ? "tundra" : "grassland";
                else if (h < 0.5) data.biomes[y][x] = lat > 0.6 ? "taiga" : "forest";
                else if (h < 0.7) data.biomes[y][x] = "mountain";
                else data.biomes[y][x] = "snow";
            }
        }
    }

    private void generateRivers(WorldData data) {
        int count = 3 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            int x = random.nextInt(data.width);
            int y = random.nextInt(data.height / 3);
            data.rivers.add(new River(x, y, 50 + random.nextInt(100)));
        }
    }

    private void generateResources(WorldData data) {
        String[] types = {"wood", "stone", "iron", "copper", "gold", "grain", "clay"};
        int count = 20 + random.nextInt(30);
        for (int i = 0; i < count; i++) {
            data.resources.add(new ResourceDeposit(
                random.nextInt(data.width),
                random.nextInt(data.height),
                types[random.nextInt(types.length)],
                50 + random.nextInt(200)
            ));
        }
    }

    private double perlinNoise(double x, double y, int octaves) {
        double value = 0, amplitude = 1, frequency = 1, max = 0;
        for (int o = 0; o < octaves; o++) {
            value += interpolatedNoise(x * frequency, y * frequency) * amplitude;
            max += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        return value / max;
    }

    private double interpolatedNoise(double x, double y) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double fx = x - ix, fy = y - iy;
        double a = hash(ix, iy), b = hash(ix + 1, iy);
        double c = hash(ix, iy + 1), d = hash(ix + 1, iy + 1);
        return lerp(lerp(a, b, fx), lerp(c, d, fx), fy);
    }

    private double hash(int x, int y) {
        long n = x * 374761393L + y * 668265263L + seed;
        n = (n ^ (n >> 13)) * 1274126177L;
        return ((n ^ (n >> 16)) & 0x7FFFFFFF) / (double) 0x7FFFFFFF;
    }

    private double lerp(double a, double b, double t) { return a + t * (b - a); }

    public static class WorldData {
        public final int width, height;
        public final double[][] heightMap;
        public final String[][] biomes;
        public final java.util.List<River> rivers = new java.util.ArrayList<>();
        public final java.util.List<ResourceDeposit> resources = new java.util.ArrayList<>();

        WorldData(int w, int h) {
            width = w; height = h;
            heightMap = new double[h][w];
            biomes = new String[h][w];
        }
    }

    public static class River {
        public final int startX, startY, length;
        River(int x, int y, int len) { startX = x; startY = y; length = len; }
    }

    public static class ResourceDeposit {
        public final int x, y, amount;
        public final String type;
        ResourceDeposit(int x, int y, String type, int amount) {
            this.x = x; this.y = y; this.type = type; this.amount = amount;
        }
    }
}
