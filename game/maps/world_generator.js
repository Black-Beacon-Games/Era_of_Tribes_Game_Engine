// Era of Tribes - Weltgenerator
// Erzeugt prozedural Karten aus Layern.

var WorldGenerator = {
    seed: 1337,
    size: { width: 1920, height: 1080 },
    layers: {},

    generate: function(seed) {
        this.seed = seed || Date.now();
        console.log("Generiere Welt mit Seed: " + this.seed);

        this.generateTerrain();
        this.generateBiomes();
        this.generateRivers();
        this.generateRoads();
        this.generateResources();
        this.generateBorders();
        this.generateDecoration();

        return this.layers;
    },

    generateTerrain: function() {
        this.layers.terrain = {
            type: "heightmap",
            data: this.perlinNoise(this.size.width, this.size.height, 6)
        };
        console.log("  Terrain generiert.");
    },

    generateBiomes: function() {
        var terrain = this.layers.terrain.data;
        this.layers.biomes = { type: "colormap", data: [] };
        for (var y = 0; y < this.size.height; y++) {
            this.layers.biomes.data[y] = [];
            for (var x = 0; x < this.size.width; x++) {
                var h = terrain[y][x];
                var latFactor = Math.abs(y / this.size.height - 0.5) * 2;
                if (h < 0.2) this.layers.biomes.data[y][x] = "water";
                else if (h < 0.35) this.layers.biomes.data[y][x] = latFactor > 0.7 ? "tundra" : "grassland";
                else if (h < 0.5) this.layers.biomes.data[y][x] = latFactor > 0.6 ? "taiga" : "forest";
                else if (h < 0.7) this.layers.biomes.data[y][x] = "mountain";
                else this.layers.biomes.data[y][x] = "snow";
            }
        }
        console.log("  Biome generiert.");
    },

    generateRivers: function() {
        this.layers.rivers = { type: "lines", data: [] };
        var riverCount = 3 + Math.floor(Math.random() * 4);
        for (var i = 0; i < riverCount; i++) {
            var startX = Math.floor(Math.random() * this.size.width);
            var startY = Math.floor(Math.random() * this.size.height * 0.3);
            this.layers.rivers.data.push({
                source: { x: startX, y: startY },
                path: this.traceRiver(startX, startY)
            });
        }
        console.log("  Fluesse generiert: " + riverCount);
    },

    generateRoads: function() {
        this.layers.roads = { type: "lines", data: [] };
        console.log("  Strassen generiert.");
    },

    generateResources: function() {
        this.layers.resources = { type: "points", data: [] };
        var resourceCount = 20 + Math.floor(Math.random() * 30);
        for (var i = 0; i < resourceCount; i++) {
            this.layers.resources.data.push({
                x: Math.floor(Math.random() * this.size.width),
                y: Math.floor(Math.random() * this.size.height),
                type: this.randomResource(),
                amount: 50 + Math.floor(Math.random() * 200)
            });
        }
        console.log("  Ressourcen generiert: " + resourceCount);
    },

    generateBorders: function() {
        this.layers.borders = { type: "colormap", data: [] };
        console.log("  Grenzen generiert.");
    },

    generateDecoration: function() {
        this.layers.decoration = { type: "points", data: [] };
        console.log("  Dekoration generiert.");
    },

    perlinNoise: function(width, height, octaves) {
        var data = [];
        for (var y = 0; y < height; y++) {
            data[y] = [];
            for (var x = 0; x < width; x++) {
                var value = 0;
                var amplitude = 1;
                var frequency = 1;
                var maxValue = 0;
                for (var o = 0; o < octaves; o++) {
                    value += this.interpolatedNoise(x * frequency, y * frequency) * amplitude;
                    maxValue += amplitude;
                    amplitude *= 0.5;
                    frequency *= 2;
                }
                data[y][x] = value / maxValue;
            }
        }
        return data;
    },

    interpolatedNoise: function(x, y) {
        var ix = Math.floor(x);
        var iy = Math.floor(y);
        var fx = x - ix;
        var fy = y - iy;
        var a = this.hash(ix, iy);
        var b = this.hash(ix + 1, iy);
        var c = this.hash(ix, iy + 1);
        var d = this.hash(ix + 1, iy + 1);
        return this.lerp(this.lerp(a, b, fx), this.lerp(c, d, fx), fy);
    },

    hash: function(x, y) {
        var n = x * 374761393 + y * 668265263 + this.seed;
        n = (n ^ (n >> 13)) * 1274126177;
        return (n ^ (n >> 16)) & 0x7FFFFFFF / 0x7FFFFFFF;
    },

    lerp: function(a, b, t) { return a + t * (b - a); },

    traceRiver: function(startX, startY) {
        var path = [{ x: startX, y: startY }];
        var x = startX, y = startY;
        var maxLength = 50 + Math.floor(Math.random() * 100);
        for (var i = 0; i < maxLength; i++) {
            var dx = (Math.random() - 0.5) * 6;
            var dy = 2 + Math.random() * 4;
            x += dx;
            y += dy;
            if (y >= this.size.height) break;
            path.push({ x: Math.floor(x), y: Math.floor(y) });
        }
        return path;
    },

    randomResource: function() {
        var resources = ["wood", "stone", "iron", "copper", "gold", "grain", "clay"];
        return resources[Math.floor(Math.random() * resources.length)];
    }
};
