# Era of Tribes

**2D Digital Board Game • Strategy • Multiplayer • Economy • Diplomacy**

Era of Tribes ist ein modernes digitales Brettspiel. Jeder Spieler übernimmt einen Stamm, erkundet die Welt, gründet Städte, baut Gebäude, handelt mit anderen Spielern und entwickelt Technologien. Das Spiel ist eine Mischung aus **Civilization**, **Catan** und **Risiko** – jedoch komplett digital und datengetrieben.

---

## Inhaltsverzeichnis

- [Vision](#vision)
- [Projektstruktur](#projektstruktur)
- [Grundlayout](#grundlayout)
- [Die Welt](#die-welt)
- [Color Map (Hex-System)](#color-map-hex-system)
- [Weltgenerator](#weltgenerator)
- [Regionen](#regionen)
- [Ressourcen](#ressourcen)
- [Städte & Gebäude](#städte--gebäude)
- [Forschung](#forschung)
- [Diplomatie](#diplomatie)
- [Handel](#handel)
- [Kampfsystem](#kampfsystem)
- [Einheiten](#einheiten)
- [Ereignisse](#ereignisse)
- [Rundenablauf](#rundenablauf)
- [UI-System](#ui-system)
- [Multiplayer](#multiplayer)
- [Steam-Integration](#steam-integration)
- [Discord-Integration](#discord-integration)
- [Karteneditor](#karteneditor)
- [Modding](#modding)
- [Build-System](#build-system)
- [Architekturprinzip](#architekturprinzip)

---

## Vision

Era of Tribes ist eine datengetriebene Strategieplattform. Fast alles – Karten, Völker, Technologien, Gebäude, Einheiten, Ereignisse und sogar die Siegbedingungen – wird über JSON-Dateien und Editoren definiert. So können neue Inhalte oder komplette Total-Conversion-Mods erstellt werden, ohne den Java-Code der Engine ändern zu müssen.

---

## Projektstruktur

```
EraOfTribes/
│
├── build.exe                          # Build-Tool
├── launcher.exe                       # Spiel-Starter
│
├── engine/                            # EraEngine-Kern
│   ├── EraEngine.jar                  # Haupt-JAR
│   ├── config/                        # Engine-Konfiguration
│   ├── steam/                         # Steam-Integration
│   ├── discord/                       # Discord-Integration
│   ├── renderer/                      # Rendering-System
│   ├── networking/                    # Netzwerk-Module
│   └── runtime/                       # Laufzeitumgebung
│
├── game/                              # Spielprojekt
│   ├── maps/                          # Weltkarten & Layer
│   ├── tribes/                        # Völker-Definitionen
│   ├── technologies/                  # Technologiebäume
│   ├── buildings/                     # Gebäude-Daten
│   ├── units/                         # Einheiten-Daten
│   ├── resources/                     # Ressourcen-Definitionen
│   ├── events/                        # Ereignis-Daten
│   ├── localization/                  # Übersetzungen (DE/EN)
│   ├── ui/                            # HUD & Menü-Definitionen
│   ├── audio/                         # Musik & Soundeffekte
│   ├── saves/                         # Spielstände
│   └── mods/                          # Modifikationen
│
├── editor/                            # Editoren
│   ├── MapEditor.exe                  # Karteneditor
│   ├── TribeEditor.exe                # Völker-Editor
│   ├── TechnologyEditor.exe           # Technologie-Editor
│   └── ScenarioEditor.exe             # Szenario-Editor
│
├── dist/                              # Build-Ausgabe
│
└── logs/                              # Log-Dateien
```

---

## Grundlayout

Die gesamte Spieloberfläche ist in feste Bereiche aufgeteilt:

```
┌─────────────────────────────────────────────────────────────────────┐
│ Spieler              Weltkarte                  Ereignisse          │
│ Ressourcen                                  Diplomatie              │
│ Forschung                                   Chat                    │
│ Gebäude                                     Handel                  │
│ Minimap                                     Rundeninfo              │
└─────────────────────────────────────────────────────────────────────┘
```

Das Spielfeld (Weltkarte) bleibt immer vollständig sichtbar. Alle Menüs und Bedienelemente befinden sich außerhalb des Kartenbereichs.

---

## Die Welt

Die komplette Karte besteht aus übereinandergelegten Bildebenen (Layern):

| Layer               | Beschreibung                  |
|---------------------|-------------------------------|
| `world_terrain.png` | Gelände (Höhen, Textur)       |
| `world_biomes.png`  | Biome (Wald, Wüste, etc.)     |
| `world_rivers.png`  | Flüsse                        |
| `world_roads.png`   | Straßen                       |
| `world_buildings.png` | Gebäude auf der Karte       |
| `world_resources.png` | Ressourcen-Vorkommen        |
| `world_borders.png` | Regions- und Staatsgrenzen    |
| `world_decoration.png` | Dekorationselemente        |

### Color Maps (Hitmaps)

Jeder relevante Layer besitzt eine zugehörige unsichtbare Color Map:

| Hitmap                      | Zweck                        |
|-----------------------------|------------------------------|
| `world_tiles.png`           | Hex-Felder (Geländetyp)      |
| `world_regions.png`         | Regionen-Zugehörigkeit        |
| `world_resources.png`       | Ressourcen-Positionen         |
| `world_buildings.png`       | Gebäude-Positionen            |

---

## Color Map (Hex-System)

Jedes Hex-Feld auf der Karte wird durch eine eindeutige Farbe identifiziert. Beim Anklicken liest die Engine den Pixel unter der Maus und ermittelt die Hex-ID.

```
Mausklick → Pixel lesen → Hex-ID (#000001 ... #001245) → Region → Objekt
```

**Vorteile:**

- Hex-Felder müssen weder Koordinaten noch ein Grid speichern
- Karten können jede beliebige Form und Größe haben
- Sechseckige, runde oder frei gezeichnete Felder sind problemlos möglich
- Keine komplexe Geometrie-Berechnung nötig

**Farbbeispiele:**

| Hex-Code  | Hex-ID | Geländetyp |
|-----------|--------|------------|
| `#000001` | 1      | Grasland   |
| `#000002` | 2      | Wald       |
| `#000003` | 3      | Gebirge    |
| `#000004` | 4      | Wasser     |
| `#000005` | 5      | Wüste      |
| `#000006` | 6      | Sumpf      |
| `#000007` | 7      | Tundra     |
| `#000008` | 8      | Stadt      |

---

## Weltgenerator

Eine Karte wird prozedural aus Layern generiert:

1. **Terrain** – Perlin-Noise-Höhenkarte
2. **Biome** – Klimazonen basierend auf Höhe und Breitengrad
3. **Flüsse** – Zufällige Quellen, die zum Meer fließen
4. **Straßen** – Verbindungen zwischen Städten
5. **Gebäude** – Platzierte Strukturen
6. **Ressourcen** – Verteilung basierend auf Gelände
7. **Grenzen** – Regions- und Staatsgrenzen
8. **Dekoration** – Bäume, Steine, visuelle Details

Der Generator verwendet einen konfigurierbaren Seed, sodass Karten reproduzierbar sind.

---

## Regionen

Jede Region auf der Weltkarte besitzt Eigenschaften:

| Eigenschaft   | Beschreibung                     |
|---------------|----------------------------------|
| Name          | Bezeichnung der Region           |
| Besitzer      | Aktuell kontrollierender Stamm   |
| Klima         | temperate, warm, cold, arid      |
| Bevölkerung   | Anzahl Einwohner                 |
| Fruchtbarkeit | 0.0 – 1.0 (Nahrungsproduktion)   |
| Stabilität    | 0 – 100 (Aufstandsrisiko)        |
| Loyalität     | 0 – 100 (Treue zum Herrscher)    |
| Kultur        | Kulturelle Prägung               |
| Wirtschaft    | Wirtschaftsleistung              |
| Militär       | Stationierte Einheiten           |
| Steuern       | Steuersatz                       |

---

## Ressourcen

| Ressource  | Typ       | Vorkommen              |
|------------|-----------|------------------------|
| Holz       | Rohstoff  | Wald, Sumpf            |
| Stein      | Rohstoff  | Gebirge, Hügel         |
| Lehm       | Rohstoff  | Grasland, Flussufer    |
| Eisen      | Rohstoff  | Gebirge                |
| Kupfer     | Rohstoff  | Gebirge, Hügel         |
| Gold       | Edel      | Gebirge                |
| Silber     | Edel      | Gebirge                |
| Getreide   | Nahrung   | Grasland               |
| Vieh       | Nahrung   | Grasland, Hügel        |
| Salz       | Luxus     | Wüste                  |
| Kohle      | Rohstoff  | Gebirge (spätes Spiel) |
| Öl         | Rohstoff  | Wüste (spätes Spiel)   |
| Gewürze    | Luxus     | Wüste                  |
| Fisch      | Nahrung   | Wasser                 |
| Wein       | Luxus     | Hügel                  |
| Wolle      | Rohstoff  | Tundra, Grasland       |

Ressourcen sind an Geländetypen gebunden und werden beim Weltgenerieren automatisch platziert.

---

## Städte & Gebäude

### Städte

Städte besitzen:

- **Einwohner** – Wachsen mit Nahrungsüberschuss
- **Gebäude** – Produktionsstätten und Verbesserungen
- **Produktion** – Herstellung von Einheiten und Gebäuden
- **Lager** – Ressourcenspeicher
- **Steuern** – Einkommen pro Runde
- **Zufriedenheit** – Beeinflusst Wachstum und Stabilität
- **Mauern** – Verteidigungsbonus
- **Garnison** – Stationierte Einheiten
- **Handelsrouten** – Passive Einnahmen

### Gebäude

| Gebäude            | Kosten                          | Effekte                                    |
|--------------------|---------------------------------|--------------------------------------------|
| Wohnhaus           | 20 Holz, 10 Stein               | +5 Bevölkerung, +1 Zufriedenheit           |
| Bauernhof          | 15 Holz                         | +3 Nahrung, +5% Wachstum                   |
| Mine               | 30 Holz, 20 Stein               | +2 Stein, +1 Eisen, +0.5 Gold              |
| Holzfällerlager    | 10 Holz                         | +3 Holz                                    |
| Marktplatz         | 25 Holz, 15 Stein               | +20% Handelseffizienz, +2 Gold             |
| Kaserne            | 30 Holz, 20 Stein, 10 Eisen     | +30% Rekrutierungsgeschwindigkeit          |
| Hafen              | 40 Holz, 30 Stein               | +1 Handelsroute, +1 Marineeinheit          |
| Kirche             | 20 Holz, 30 Stein, 10 Gold      | +2 Glaube, +2 Zufriedenheit, +1 Stabilität |
| Akademie           | 40 Stein, 30 Gold               | +3 Forschung                               |
| Schmiede           | 20 Stein, 15 Eisen              | +1 Angriff, +1 Produktion                  |
| Stadtmauer         | 50 Stein, 20 Holz               | +3 Verteidigung, +2 Garnison               |
| Schloss            | 80 Stein, 40 Gold, 20 Eisen     | +5 Verteidigung, +2 Stabilität, +2 Prestige|
| Wachturm           | 15 Stein, 10 Holz               | +3 Sichtweite, +1 Warnzeit                 |
| Werft              | 50 Holz, 25 Stein, 15 Gold      | +1 Schiffsgeschwindigkeit, +1 Schiffsangriff|

---

## Forschung

### Epochen

- **Bronzezeit** – Grundlegende Technologien
- **Eisenzeit** – Fortgeschrittene Militär- und Wirtschaftstechnologien
- **Mittelalter** – High-End-Technologien

### Technologien

| Technologie         | Era      | Kosten | Voraussetzung        | Effekte                               |
|---------------------|----------|--------|----------------------|---------------------------------------|
| Landwirtschaft      | Bronze   | 50     | –                    | +2 Nahrung, +10% Bevölkerungswachstum |
| Schrift             | Bronze   | 80     | Landwirtschaft       | +1 Forschung, +10% Handelseffizienz   |
| Bronzeverarbeitung  | Bronze   | 100    | Bergbau              | +1 Angriff, +1 Produktion             |
| Bergbau             | Bronze   | 60     | –                    | +2 Produktion, +10% Rohstoffertrag    |
| Navigation          | Bronze   | 120    | Schrift              | +2 Handelsreichweite, +1 Marinebewegung|
| Eisenverarbeitung   | Eisen    | 200    | Bronzeverarbeitung   | +2 Angriff, +1 Verteidigung           |
| Währung             | Eisen    | 150    | Schrift              | +20% Handelseffizienz, +15% Steuern   |
| Belagerung          | Eisen    | 250    | Eisenverarbeitung    | +3 Belagerungsangriff                 |
| Philosophie         | Eisen    | 180    | Schrift              | +2 Forschung, +1 Stabilität           |
| Ingenieurswesen     | Eisen    | 220    | Bergbau, Währung     | +2 Produktion, +20% Baugeschwindigkeit|
| Feudalismus         | Mittel   | 400    | Eisenverarbeitung, Philosophie | +2 Verteidigung, +15% Bevölkerung|
| Handelsrouten       | Mittel   | 350    | Währung, Navigation  | +2 Handelsrouten, 5 Gold/Route        |
| Katapult            | Mittel   | 300    | Belagerung, Ingenieurswesen | +5 Belagerungsangriff          |
| Gesetzbuch          | Mittel   | 280    | Schrift, Philosophie | +2 Stabilität, +20% Steuern, +1 Loyalität|

---

## Diplomatie

| Aktion               | Effekt                                  |
|----------------------|-----------------------------------------|
| Krieg erklären       | –100 Beziehung, offener Konflikt        |
| Frieden schließen    | +30 Beziehung, Waffenstillstand         |
| Nichtangriffspakt    | Frieden für 20 Runden                   |
| Bündnis              | Gemeinsame Kriege, gegenseitiger Schutz |
| Handelsvertrag       | +1 Handelsroute, verbesserte Preise     |
| Tribut fordern/zahlen| Gold gegen Frieden oder Bündnis         |
| Grenzvertrag         | Definierte Einflusszonen                |
| Gemeinsame Forschung | Geteilte Forschungspunkte               |

---

## Handel

Vollständig integriertes Handelssystem.

**Handelsobjekte:**

- Ressourcen (Holz, Stein, Eisen, Gold, etc.)
- Gebäude (direkter Kauf/Verkauf)
- Gebiete (Regionen kaufen/tauschen)
- Einheiten (kaufen/verkaufen)
- Technologien (gegen Gold oder andere Techs)
- Verträge (diplomatische Vereinbarungen)

Der Handel läuft über ein separates Handelsfenster mit eigener Hitmap.

---

## Kampfsystem

Einheiten besitzen folgende Attribute:

| Attribut       | Beschreibung                              |
|----------------|-------------------------------------------|
| Lebenspunkte   | Gesundheit der Einheit                    |
| Moral          | 0–100, beeinflusst Kampfverhalten         |
| Erfahrung      | Steigert Angriff/Verteidigung             |
| Angriff        | Grundschaden im Kampf                     |
| Verteidigung   | Schadensreduktion                         |
| Reichweite     | Angriffsdistanz in Hex-Feldern            |
| Bewegung       | Bewegungsweite pro Runde                  |
| Spezialfähigkeit| Einzigartige Kampffähigkeit              |

**Kampfablauf pro Runde:**

1. Angreifer wählt Zielfeld
2. Kampf wird in Runden ausgetragen
3. Pro Runde greifen alle Einheiten an
4. Moral-Check nach jeder Runde
5. Sieger oder Rückzug

---

## Einheiten

| Einheit         | Typ      | HP | Angriff | Verteidigung | Bewegung | Reichweite | Spezial           |
|-----------------|----------|----|---------|--------------|----------|------------|-------------------|
| Bauer           | Zivil    | 5  | 0       | 0            | 1        | 0          | Arbeit            |
| Siedler         | Zivil    | 5  | 0       | 0            | 2        | 0          | Stadt gründen     |
| Arbeiter        | Zivil    | 5  | 0       | 1            | 2        | 0          | Straße bauen      |
| Speerkämpfer    | Land     | 15 | 4       | 3            | 2        | 1          | Phalanx           |
| Bogenschütze    | Land     | 10 | 5       | 1            | 2        | 3          | Salve             |
| Schwertkämpfer  | Land     | 20 | 6       | 4            | 2        | 1          | –                 |
| Reiter          | Land     | 18 | 5       | 2            | 4        | 1          | Ansturm           |
| Katapult        | Land     | 12 | 8       | 1            | 1        | 4          | Belagerung        |
| Trireme         | Marine   | 25 | 4       | 3            | 3        | 1          | Rammstoß          |
| Langschiff      | Marine   | 20 | 5       | 2            | 4        | 1          | Überfall          |
| Händler         | Zivil    | 5  | 0       | 0            | 2        | 0          | Handel            |
| Späher          | Land     | 8  | 2       | 1            | 5        | 2          | Kundschaft        |
| Legionär        | Land     | 25 | 7       | 6            | 2        | 1          | Formation         |
| Plünderer       | Land     | 12 | 5       | 2            | 3        | 1          | Plündern          |
| Streitwagen     | Land     | 16 | 6       | 2            | 4        | 2          | Scharmützel       |
| Berserker       | Land     | 22 | 8       | 2            | 2        | 1          | Raserei           |

---

## Ereignisse

Die Engine unterstützt verschiedene Ereignistypen, die zufällig oder bedingt ausgelöst werden:

| Ereignis           | Typ        | Auswirkung                                       |
|--------------------|------------|--------------------------------------------------|
| Hungersnot         | Natur      | –10 Bevölkerung, –15 Zufriedenheit, –20 Nahrung  |
| Feuersbrunst       | Natur      | –5 Bevölkerung, –1 Gebäude, –10 Gold             |
| Aufstand           | Gesellschaft| –10 Stabilität, –3 Bevölkerung, –5 Gold          |
| Pest               | Natur      | –20 Bevölkerung, –20 Zufriedenheit               |
| Piraten            | Militär    | –15 Gold, –1 Handelsroute                        |
| Erdbeben           | Natur      | –2 Gebäude, –8 Bevölkerung                       |
| Dürre              | Natur      | –15 Nahrung, –20% Bevölkerungswachstum           |
| Überschwemmung     | Natur      | –3 Bevölkerung, –1 Gebäude, –3 Bauernhof         |
| Rebellion          | Militär    | –15 Stabilität, –5 Militär, –10 Gold             |
| Goldfund           | Entdeckung | +50 Gold, +5 Zufriedenheit                       |
| Karawane           | Wirtschaft | +1 Handelsroute, +10 Gold                        |
| Kulturelle Blüte   | Gesellschaft| +5 Forschung, +5 Zufriedenheit, +2 Prestige     |

---

## Rundenablauf

Jede Runde durchläuft folgende Phasen:

```
Rundenstart
    ↓
Steuern (Einnahmen berechnen)
    ↓
Produktion (Ressourcen & Gebäude)
    ↓
Forschung (Technologiepunkte sammeln)
    ↓
Handel (Spieler-Zug)
    ↓
Bewegung (Spieler-Zug)
    ↓
Kampf (automatische Auflösung)
    ↓
Ereignisse (Zufallsereignisse auswürfeln)
    ↓
Rundenende
```

Die Phasen **Handel** und **Bewegung** sind Spieler-Züge; alle anderen laufen automatisch ab.

---

## UI-System

Die komplette Oberfläche besteht aus Bildern und Hitmaps:

```
hud.png           → Sichtbares HUD
hud_hitmap.png    → Unsichtbare Klick-Zonen
```

**Beispiel HUD-Buttons:**

| Farbe     | Aktion       |
|-----------|--------------|
| Rot       | Gebäude      |
| Blau      | Forschung    |
| Gelb      | Diplomatie   |
| Grün      | Handel       |
| Pink      | Menü         |

### Hauptmenü

```
Neues Spiel
Spiel laden
Mehrspieler
Kampagne
Karteneditor
Mods
Einstellungen
Credits
Beenden
```

### Fertige UI-Komponenten

- **Minimap** – Übersichtskarte (bottom-left)
- **Player Panel** – Stammesinfo (top-left)
- **Resource Bar** – Ressourcenleiste (top)
- **Event Log** – Ereignis-Log (top-right)
- **Diplomacy Panel** – Diplomatie-Übersicht (right)
- **Chat** – Nachrichtensystem (bottom-right)
- **Turn Info** – Rundenanzeige (bottom-center)
- **Notification** – Benachrichtigungen (center-top)
- **Tooltip** – Hilfetexte
- **Popup Window** – Dialogfenster
- **Console** – Entwickler-Konsole (F1)

---

## Multiplayer

| Feature              | Beschreibung                              |
|----------------------|-------------------------------------------|
| Steam-Lobbys         | Über Steam Freunde einladen               |
| Private Spiele       | Nicht-öffentliche Partien                 |
| Passwortgeschützt    | Mit Passwort schützen                     |
| Hotseat-Modus        | Mehrere Spieler an einem PC               |
| LAN                  | Lokales Netzwerk                          |
| Dedicated Server     | Eigenständiger Spielserver                |
| Zuschauer-Modus     | Beobachten ohne Eingriff                  |
| Reconnect            | Wiederverbindung nach Verbindungsabbruch  |

---

## Steam-Integration

- Achievements (8 bereits definiert)
- Cloud Saves
- Workshop (für Mods und Karten)
- Freundesliste & Einladungen
- Statistiken
- Leaderboards

---

## Discord-Integration

- **Rich Presence** – Zeigt "Spielt Era of Tribes – 4/8 Spieler – Runde 27 – Karte Europa"
- **Join Game** – Beitreten über Discord
- **Spectator** – Zuschauen über Discord

---

## Karteneditor

Ein separates Tool zum Erstellen von Karten:

- Terrain malen (Pinsel, Füllung, verschiedene Formen)
- Color-Map automatisch erzeugen
- Regionen definieren (Grenzen zeichnen)
- Ressourcen platzieren
- Flüsse zeichnen (Quelle bis Mündung)
- Straßen erstellen
- Städte setzen
- Startpositionen für Völker festlegen
- Vorschau mit Live-Test
- Export nach `game/maps/`

### Weitere Editoren

- **TribeEditor** – Völker-Eigenschaften, Boni, Startbedingungen, Anführer
- **TechnologyEditor** – Technologiebäume, Epochen, Kosten, Effekte, Voraussetzungen
- **ScenarioEditor** – Komplette Szenarien mit Karte, Völkern, Start- und Siegbedingungen

---

## Modding

Ein Mod kann hinzufügen:

- Neue Völker
- Neue Technologien
- Neue Gebäude
- Neue Ressourcen
- Neue Karten
- Neue Musik
- Neue Ereignisse
- Neue Einheiten
- Neue Siegbedingungen

**Beispiel-Struktur:**

```
game/mods/MeinMod/
├── mod.json                         # Mod-Info
├── tribes/perser.json               # Neues Volk
├── technologies/persian_techs.json  # Neue Techs
├── buildings/persian_buildings.json # Neue Gebäude
├── units/persian_units.json         # Neue Einheiten
└── scripts/perser_init.js           # Initialisierung
```

---

## Build-System

`build.exe` durchläuft:

1. **Projekt validieren**
2. **Karten validieren** (world.json, Hitmaps)
3. **Assets optimieren und komprimieren** (Bilder, Audio, JSON)
4. **Spielpaket erstellen** → `dist/`
5. **Plattform-Builds** (Windows x64, Linux x64, macOS Universal)
6. **Steam-Build vorbereiten**
7. **Build-Report** → `logs/build.log`

---

## Architekturprinzip

**Datengesteuert statt hartcodiert.** Die EraEngine kennt keine Völker, Technologien, Gebäude oder Einheiten – sie stellt nur die Systeme bereit. Das gesamte Spiel wird durch JSON-Definitionen und JavaScript-Skripte beschrieben.

Dadurch können mit derselben Engine komplett unterschiedliche Spiele, Szenarien und Mods umgesetzt werden, ohne den Java-Code anzufassen.

---

## Erweiterungen (geplant)

- KI-Spieler mit mehreren Schwierigkeitsgraden
- Replay-System
- Undo/Redo
- Steam Workshop-Integration
- Cloud-Synchronisation
- Controller- und Touchscreen-Unterstützung
- Theme-System
- In-Game-Konsole
- Script-Hot-Reload

---

## Lizenz

*Noch festzulegen.*

---

## Mitwirken

Beiträge sind willkommen – ob als Entwickler, Designer, Tester oder Übersetzer.
