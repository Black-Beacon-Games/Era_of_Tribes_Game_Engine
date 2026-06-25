# Era of Tribes

**2D Digital Board Game – Strategy – Multiplayer – Economy – Diplomacy**

Era of Tribes ist ein datengetriebenes Strategiespiel. Karten, Völker, Technologien, Gebäude, Einheiten und Ereignisse werden über JSON definiert – der Java-Kern bleibt generisch.

## Projektstruktur

```
engine/              EraEngine-Kern (JAR, Konfiguration)
game/                Spielprojekt (Daten, Assets, Maps)
├── assets/          Bilder, Audio
├── tribes/          Völker-Definitionen
├── technologies/    Technologiebäume
├── buildings/       Gebäude-Daten
├── units/           Einheiten-Daten
├── resources/       Ressourcen
├── events/          Ereignisse
├── maps/            Weltkarten
├── ui/              HUD-Definitionen
├── localization/    Übersetzungen (DE/EN)
├── audio/           Musik & Sound
├── saves/           Spielstände
└── mods/            Modifikationen
src/                 Java-Quellcode
build.gradle         Gradle-Build
launcher.bat         Starter (Windows)
```

## Build & Start

**Build:**
```
gradlew build
```

**Starten:**
```
launcher.bat
```
Oder direkt:
```
java -jar engine/EraEngine.jar --game game/
```

## Architektur

Datengesteuert: Die Engine stellt nur Systeme bereit (Rendering, Netzwerk, Audio, UI). Das gesamte Spiel wird durch JSON-Definitionen beschrieben – neue Inhalte oder Mods ohne Java-Code.

- Rendering: Swing (Java AWT)
- Netzwerk: Eigene IPC/Steam-Lobby
- Discord: Rich Presence via Named Pipe
- UI: Bildbasiert (HUD-PNG + definierte Klick-Zonen)
- Skripte: JavaScript-Integration geplant

## Abhängigkeiten

- Java 17+
- Gson 2.11
- SLF4J + Logback
- JNA 5.14 (Discord-Integration)
