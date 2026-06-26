# 🌍 Era of Tribes

> **2D Digital Board Game – Strategy – Multiplayer – Economy – Diplomacy**

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java)
![Build](https://img.shields.io/badge/build-passing-brightgreen?logo=gradle)
![Version](https://img.shields.io/badge/version-1.0.0-blue)

Era of Tribes ist ein **datengesteuertes Strategiespiel**: Karten, Völker, Technologien, Gebäude, Einheiten und Ereignisse werden über JSON definiert – der Java-Kern bleibt generisch und modular erweiterbar.

---

## 🚀 Quick Start

```bash
gradlew build       # kompilieren + JAR packen
launcher.bat        # Spiel starten
```

---

## 📁 Projektstruktur

```
Engine/
├── engine/                  EraEngine-Kern (JAR, Konfiguration)
├── game/                    Spielprojekt (Daten, Assets, Maps)
│   ├── assets/              Bilder
│   ├── audio/               Musik & Sound (.wav / .mp3)
│   ├── buildings/           Gebäude-Daten
│   ├── config/              discord.json
│   ├── events/              Ereignisse
│   ├── localization/        Übersetzungen (DE/EN)
│   ├── maps/                Weltkarten
│   ├── mods/                Modifikationen
│   ├── resources/           Ressourcen
│   ├── saves/               Spielstände
│   ├── technologies/        Technologiebäume
│   ├── tribes/              Völker-Definitionen
│   ├── ui/                  HUD-Definitionen
│   └── units/               Einheiten-Daten
├── src/main/java/com/eraoftribes/
│   ├── engine/              Engine-Kern
│   │   ├── asset/           Asset-Manager (JSON, Bilder, SVGs)
│   │   ├── audio/           Audio-Engine (.wav / .mp3)
│   │   ├── discord/         Discord Rich Presence
│   │   ├── input/           Eingabe-Manager
│   │   ├── module/          Modul-System
│   │   ├── network/         Netzwerk-Layer
│   │   ├── rendering/       Renderer (Swing)
│   │   ├── save/            Speicherverwaltung
│   │   ├── scene/           Scene-Manager
│   │   ├── script/          JS-Script-Engine
│   │   ├── ui/              UI-Engine
│   │   └── world/           Weltgenerator
│   └── game/                Spiel-Logik
│       ├── building/
│       ├── combat/
│       ├── diplomacy/
│       ├── event/
│       ├── map/
│       ├── resource/
│       ├── scene/           MainMenu, Settings, Credits, Loading
│       ├── tech/
│       └── tribe/
├── build.gradle             Gradle-Build (Java 17)
├── launcher.bat             Starter (Windows)
└── README.md
```

---

## 🧱 Architektur

Datengesteuert: Die Engine stellt Systeme bereit, das Spiel wird durch JSON beschrieben – Mods ohne Java-Code.

| System | Technologie |
|--------|-------------|
| 🎨 **Rendering** | Swing (Java AWT) |
| 🔊 **Audio** | `javax.sound.sampled` + MP3SPI (`.wav` / `.mp3`) |
| 🌐 **Netzwerk** | Eigene IPC / Steam-Lobby |
| 🎮 **Discord** | Rich Presence via Named Pipe (JNA) |
| 🖼️ **UI** | Bildbasiert (HUD-PNG + Klick-Zonen) |
| 📜 **Skripte** | JavaScript-Integration geplant |

### 🎬 Szenen-System

`SceneManager` wechselt per `switchTo("name")`. Jede Szene hat `onEnter()`, `update(dt)`, `render(r)`:

| Schlüssel | Klasse | Beschreibung |
|-----------|--------|-------------|
| `loading` | `LoadingScene` | Ladebildschirm mit Zitaten |
| `main_menu` | `MainMenuScene` | Hauptmenü |
| `settings` | `SettingsScene` | Einstellungen |
| `credits` | `CreditsScene` | Credits |
| `game` | `Scene("game")` | Hauptspiel |
| `lobby` | `Scene("lobby")` | Lobby |
| `map_editor` | `Scene("map_editor")` | Karteneditor |

---

## 📦 Abhängigkeiten

| Dependency | Version | Zweck |
|-----------|---------|-------|
| Java | 17+ | Runtime / Kompilierung |
| Gson | 2.11 | JSON-Parsing |
| SLF4J + Logback | 2.0.16 / 1.5.13 | Logging |
| JNA + JNA-Platform | 5.14 | Discord Native Pipe |
| Batik Transcoder | 1.17 | SVG-Rendering |
| MP3SPI | 1.9.5 | MP3-Decoding (`.mp3`) |
| JUnit Jupiter | 5.11 | Tests |

---

## 🗺️ Roadmap

### ✅ Abgeschlossen

#### Audio-Engine (`v1.0`)

| Feature | Status | Details |
|---------|--------|---------|
| `.wav` / `.mp3` Wiedergabe | ✅ | via `javax.sound.sampled` + MP3SPI |
| SFX (einmalig) | ✅ | Thread-Pool, parallel spielbar |
| Musik (Loop) | ✅ | Eigener Thread, nahtlose Loops |
| Volume-Regelung | ✅ | Master / Music / SFX getrennt |
| Volume live updaten | ✅ | Wirkt sofort auf aktive SFX + Musik |
| Fade-out beim Stop | ✅ | 300ms gleitender Übergang |
| `clearTracks()` | ✅ | Tracks entladen + Playback stoppen |
| JUnit-Tests | ✅ | 14 Tests |

<details>
<summary><b>🔊 API-Referenz (AudioEngine)</b></summary>

```java
// Track registrieren
audio.loadTrack("id", "pfad/datei.wav", loop, volume);

// Abspielen
audio.play("id");              // SFX oder Musik (auto anhand loop-Flag)

// Stoppen
audio.stop("id");              // Einzelnen Track
audio.stopAll();               // Alles stoppen

// Lautstärke (0.0 – 1.0)
audio.setVolume(0.8);          // Master
audio.setMusicVolume(0.7);     // Musik
audio.setSFXVolume(1.0);       // SFX

// Szenenwechsel
audio.clearTracks();           // Alle Tracks entfernen + stoppen
```
</details>

---

### 🔄 In Arbeit / Priorität

| Bereich | Feature | Status |
|---------|---------|--------|
| 🔊 Audio | Pfad-Vereinheitlichung über `gamePath` | 🔜 Geplant |
| 🔊 Audio | Preload-Puffer für perfekte Loops | 🔜 Geplant |
| 🔊 Audio | Szenen-Lebenszyklus (auto `clearTracks`) | 🔜 Geplant |
| 🎮 Gameplay | Runden-basierte Phasen (Taxen, Produktion, …) | ✅ Basis |
| 🧠 AI | Gegner-KI (Wirtschaft, Militär, Diplomatie) | 🔜 Geplant |
| 🌐 Multiplayer | Lobby-System + Steam-Integration | 🔜 Geplant |
| 🗺️ World | Prozedurale Weltgenerierung | 🔜 Geplant |
| 🖼️ UI | Bildschirm-Übergänge + Animationen | 🔜 Geplant |

### 💭 Langfristig

- **3D / Positional Audio** – räumlicher Sound für Einheiten/Kamera
- **JavaScript-Scripting** – Events und KI per JS definierbar
- **Modding-API** – komplette Inhalts-Mods ohne Java-Code
- **Cloud-Saves** – Spielstand-Synchronisation

---

## 🛠️ Build im Detail

```bash
gradlew clean build            # sauberer Build
gradlew test                   # Tests ausführen
gradlew runGame                # bauen + direkt starten
```

**Build-Phasen:**

```
compileJava → jar → dist → engine/EraEngine.jar
```

Nach dem Build liegt das ausführbare JAR unter `engine/EraEngine.jar`.

---

## ❓ Troubleshooting

### Credits-Button fehlt im Hauptmenü

**Ursache:** Alte `.class`-Datei / altes JAR – Build nicht durchgelaufen.

```bash
gradlew clean build
```

Prüfen:
```bash
# JAR auf Aktualität prüfen
Test-Path engine/EraEngine.jar
Get-Item engine/EraEngine.jar | Select-Object LastWriteTime, Length
```

### `UP-TO-DATE` trotz Code-Änderung

```bash
gradlew clean compileJava      # Kompilierung erzwingen
```

### `EraEngine.jar` nicht gefunden

```bash
gradlew clean build
```

---

## 🏆 Credits

**Game Design** – Arne Lorenz  
**Software Engineering & Web Development** – Kilian Bogus

---

*© Black Beacon Games*
