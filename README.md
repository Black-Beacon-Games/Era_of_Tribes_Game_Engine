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
├── main/java/com/eraoftribes/
│   ├── engine/           Engine-Kern
│   │   ├── rendering/    Renderer (Swing)
│   │   ├── scene/        Scene-Manager
│   │   ├── ui/           UI-Engine
│   │   ├── asset/        Asset-Manager (JSON, Bilder, SVGs)
│   │   ├── audio/        Audio-Engine
│   │   ├── discord/      Discord Rich Presence
│   │   ├── network/      Netzwerk-Layer
│   │   ├── module/       Modul-System
│   │   └── input/        Eingabe-Manager
│   └── game/             Spiel-Logik
│       └── scene/        Spiel-Szenen
│           ├── MainMenuScene.java   Hauptmenü (Titel, Buttons)
│           ├── SettingsScene.java   Einstellungen (Registerkarten)
│           ├── CreditsScene.java    Credits-Anzeige
│           └── LoadingScene.java    Ladebildschirm
build.gradle         Gradle-Build-Datei
launcher.bat         Starter (Windows)
```

## Build & Start

### Build ausführen

```
gradlew build
```

Dieser Befehl durchläuft folgende Phasen:

1. **`compileJava`** – Alle `.java`-Dateien aus `src/` werden in `.class`-Dateien unter `build/classes/java/main/` übersetzt.
2. **`processResources`** – Ressourcen-Dateien werden kopiert (sofern vorhanden).
3. **`classes`** – Führt compileJava + processResources zusammen.
4. **`jar`** – Alle `.class`-Dateien + Abhängigkeiten (Gson, Logback, JNA usw.) werden in ein JAR gepackt: `build/libs/EraOfTribes-1.0.0.jar`.
5. **`dist`** – Das JAR wird nach `engine/EraEngine.jar` kopiert (siehe `build.gradle`, Zeile 49–54).

### Spiel starten

```
launcher.bat
```

Die `launcher.bat` führt folgenden Befehl aus:

```
java -jar engine/EraEngine.jar --game game/ --log logs/
```

- `--game game/` – Gibt den Pfad zum Spielverzeichnis mit JSONs und Assets an.
- `--log logs/` – Schreibt Logs in den `logs/`-Ordner.

Alternativ direkt:

```
java -jar engine/EraEngine.jar --game game/
```

---

## Architektur

Datengesteuert: Die Engine stellt nur Systeme bereit (Rendering, Netzwerk, Audio, UI). Das gesamte Spiel wird durch JSON-Definitionen beschrieben – neue Inhalte oder Mods ohne Java-Code.

- **Rendering:** Swing (Java AWT)
- **Netzwerk:** Eigene IPC/Steam-Lobby
- **Discord:** Rich Presence via Named Pipe
- **UI:** Bildbasiert (HUD-PNG + definierte Klick-Zonen)
- **Skripte:** JavaScript-Integration geplant

### Szenen-System

Das Spiel verwendet ein Scene-System (`com.eraoftribes.engine.scene`). Jede Szene hat `onEnter()`, `update(dt)` und `render(r)`. Der `SceneManager` wechselt zwischen Szenen per `switchTo("name")`.

**Registrierte Szenen** (in `Game.java`, Zeile 64–74):

| Schlüssel      | Klasse            | Beschreibung                |
|----------------|-------------------|-----------------------------|
| `loading`      | `LoadingScene`    | Ladebildschirm mit Zitaten  |
| `main_menu`    | `MainMenuScene`   | Hauptmenü                   |
| `settings`     | `SettingsScene`   | Einstellungen               |
| `credits`      | `CreditsScene`    | Credits                     |
| `game`         | `Scene("game")`   | Hauptspiel (anonyme Klasse) |
| `lobby`        | `Scene("lobby")`  | Lobby                       |
| `map_editor`   | `Scene("map_editor")` | Karteneditor            |

### Hauptmenü im Detail (`MainMenuScene.java`)

Die Buttons werden **nicht** aus einer JSON geladen, sondern sind direkt im Java-Code definiert:

```java
// Zeile 12 – Die Button-Beschriftungen als String-Array
private static final String[] LABELS = {"Play", "Settings", "Credits", "Quit"};
```

**Ablauf beim Rendern** (Zeile 33–67):

1. `r.getWidth()` / `r.getHeight()` – Aktuelle Fenstergröße ermitteln.
2. Titel "ERA OF TRIBES" und Version "v1.0.0" mittig zeichnen.
3. `startY = h * 0.4` – Y-Position des ersten Buttons (40% der Fensterhöhe).
4. Schleife über `LABELS`:
   - Jeder Button ist `btnW = 220` × `btnH = 50` Pixel groß.
   - Abstand zwischen Buttons: `gap = 12` Pixel.
   - Y-Position: `by = startY + i * (btnH + gap)`.
   - Bei 4 Buttons ergibt das: `startY`, `startY+62`, `startY+124`, `startY+186`.
   - Bei 1080p sind die Buttons ca. bei Y=432, 494, 556, 618 – alle gut sichtbar.
5. Maus-Hover: Wenn Maus über einem Button, wird dieser blau hervorgehoben.
6. Tastatur-Navigation: Pfeiltasten/Pfeiltasten oder W/S ändert `selected`.
7. Klick/Auslösen ruft `activate()` auf.

**`activate()`-Methode** (Zeile 69–76):

```java
switch (selected) {
    case 0 -> engine.getSceneManager().switchTo("game");      // Play
    case 1 -> engine.getSceneManager().switchTo("settings");   // Settings
    case 2 -> engine.getSceneManager().switchTo("credits");    // Credits
    case 3 -> System.exit(0);                                  // Quit
}
```

Jeder Index entspricht genau dem Eintrag im `LABELS`-Array.

**CreditsScene** (`CreditsScene.java`):
- Zeigt zwei Textzeilen an: "Game Design by Arne Lorenz" und "Software Engineering & Web Development by Kilian Bogus".
- Hat einen "Back"-Button, der mit `switchTo("main_menu")` zurück zum Hauptmenü führt.
- Wurde über `register("credits", new CreditsScene(engine))` in `Game.java` registriert.

---

## Abhängigkeiten

- Java 17+ (build.gradle: `sourceCompatibility = JavaVersion.VERSION_17`)
- Gson 2.11 – JSON-Parsing für Spiel-Definitionen
- SLF4J + Logback – Logging
- JNA 5.14 – Discord-Integration (Native Pipe)
- Batik Transcoder 1.17 – SVG-Rendering
- JUnit Jupiter 5.11 – Tests

---

## Credits

**Game Design by Arne Lorenz**
**Software Engineering & Web Development by Kilian Bogus**

---

## Häufige Build-Probleme (Troubleshooting)

### 1. Credits-Button fehlt im Hauptmenü

**Symptom:**  
Im Hauptmenü werden nur "Play", "Settings" und "Quit" angezeigt (drei statt vier Buttons). Der "Credits"-Button ist nicht zu sehen, obwohl die `MainMenuScene.java` auf der Festplatte `"Credits"` im `LABELS`-Array enthält.

---

#### Ursache 1 – Quellcode wurde nicht kompiliert

**Problem:**  
Die Datei `MainMenuScene.java` wurde lokal bearbeitet (z. B. `LABELS` von `{"Play", "Settings", "Quit"}` auf `{"Play", "Settings", "Credits", "Quit"}` geändert), aber das Projekt wurde **nicht neu gebaut**. Das Spiel startet dann mit der alten `.class`-Datei aus `build/classes/java/main/`, die noch den alten Code ohne "Credits" enthält.

**Erklärung der Build-Kette:**

```
MainMenuScene.java  (Quellcode)
        │
        │  compileJava (Gradle-Task)
        ▼
MainMenuScene.class  (Bytecode in build/classes/.../)
        │
        │  jar (Gradle-Task)
        ▼
EraOfTribes-1.0.0.jar  (alle .class-Dateien + Abhängigkeiten in build/libs/)
        │
        │  dist (Gradle-Task)
        ▼
EraEngine.jar  (Kopie in engine/)
        │
        │  launcher.bat: java -jar engine/EraEngine.jar --game game/
        ▼
Spiel läuft
```

Wenn Schritt 1 (`compileJava`) nicht durchgeführt wird, enthält die `.class`-Datei nicht den neuen Code. Wenn Schritt 4 (`dist`) fehlschlägt, wird zwar neu kompiliert, aber das alte JAR in `engine/` bleibt liegen.

**Lösung:**  
Vollständigen Clean-Build durchführen – damit werden alle alten Build-Artefakte gelöscht und alles neu erstellt:

```
gradlew clean build
```

- `clean` löscht das gesamte `build/`-Verzeichnis.
- `build` führt compileJava → jar → dist aus.
- Danach `launcher.bat` ausführen.

**Manuelle Prüfung, ob die Kompilierung funktioniert hat:**

```
# Nach dem Build: Prüfen, ob "Credits" in der .class-Datei vorkommt
Select-String -Path build/classes/java/main/com/eraoftribes/game/scene/MainMenuScene.class -Pattern "Credits"
```

Ausgabe sollte `Credits` enthalten. Alternativ die `.java`-Datei mit dem Commit vergleichen:

```
git diff HEAD -- src/main/java/com/eraoftribes/game/scene/MainMenuScene.java
```

Erwartete Ausgabe:

```diff
- private static final String[] LABELS = {"Play", "Settings", "Quit"};
+ private static final String[] LABELS = {"Play", "Settings", "Credits", "Quit"};
...
- case 2 -> System.exit(0);
+ case 2 -> engine.getSceneManager().switchTo("credits");
+ case 3 -> System.exit(0);
```

---

#### Ursache 2 – `dist`-Task kopiert JAR nicht (Build-Tool-Bug im Original)

**Problem:**  
Der `dist`-Task in `build.gradle` war ursprünglich so definiert:

```groovy
// FEHLERHAFTE Version:
tasks.register('dist', Copy) {
    dependsOn jar
    from layout.buildDirectory.dir("libs/${rootProject.name}.jar")  // sucht nach EraOfTribes.jar
    into layout.projectDirectory.dir('engine')
    rename "${rootProject.name}.jar", 'EraEngine.jar'
}
```

`rootProject.name` ist in `settings.gradle` (Zeile 1) als `EraOfTribes` definiert.  
`version` ist in `build.gradle` (Zeile 6) als `'1.0.0'` definiert.

Gradle benennt das JAR nach dem Schema `${rootProject.name}-${version}.jar`, also `EraOfTribes-1.0.0.jar`.

Der `dist`-Task suchte aber nach `${rootProject.name}.jar`, also `EraOfTribes.jar` (ohne Version).  
Da diese Datei nicht existiert, **wurde nichts kopiert**. Das alte `engine/EraEngine.jar` blieb unverändert liegen – und enthielt noch den Code ohne Credits-Button.

**Warum war überhaupt eine alte `engine/EraEngine.jar` vorhanden?**  
Wahrscheinlich wurde sie bei einem früheren Build manuell oder mit einer anderen Konfiguration dorthin kopiert, oder der `dist`-Task hat zu einem früheren Zeitpunkt funktioniert (bevor die `version` in `build.gradle` gesetzt wurde).

**Lösung (seit Commit `...` gefixt):**  
Der `dist`-Task wurde korrigiert, indem die Versionsnummer im Quellpfad berücksichtigt wird:

```groovy
// KORRIGIERTE Version (seit v1.0.0):
tasks.register('dist', Copy) {
    dependsOn jar
    from layout.buildDirectory.dir("libs/${rootProject.name}-${project.version}.jar")
    into layout.projectDirectory.dir('engine')
    rename "${rootProject.name}-${project.version}.jar", 'EraEngine.jar'
}
```

Damit wird `build/libs/EraOfTribes-1.0.0.jar` → `engine/EraEngine.jar` kopiert.

**Alternative Lösung (wenn der Fix nicht angewendet werden kann):**  
Manuelles Kopieren nach dem Build:

```
Copy-Item build/libs/EraOfTribes-1.0.0.jar engine/EraEngine.jar -Force
```

Dann mit `launcher.bat` starten.

---

#### Diagnose: Welche der beiden Ursachen liegt vor?

**Schritt 1 – Prüfen, ob die .class-Datei aktuell ist:**

Vergleiche die Zeitstempel der `.java`- und `.class`-Dateien:

```
Get-Item src/main/java/com/eraoftribes/game/scene/MainMenuScene.java  | Select-Object LastWriteTime
Get-Item build/classes/java/main/com/eraoftribes/game/scene/MainMenuScene.class | Select-Object LastWriteTime
```

- Wenn `.class` älter ist als `.java` → **Ursache 1** (nicht kompiliert).
- Wenn `.class` neuer/gleich alt ist wie `.java` → Kompilierung OK → weiter zu Schritt 2.

**Schritt 2 – Prüfen, ob das JAR aktuell ist:**

```
Get-Item build/libs/EraOfTribes-1.0.0.jar | Select-Object LastWriteTime
Get-Item engine/EraEngine.jar | Select-Object LastWriteTime
```

- Wenn `EraEngine.jar` älter ist als `EraOfTribes-1.0.0.jar` → **Ursache 2** (dist hat nicht kopiert).
- Wenn beide gleich alt sind → alles OK, der Fehler liegt woanders.

**Schritt 3 – Prüfen, ob das JAR "Credits" enthält:**

```
& "C:\Program Files\Java\jdk-XX\bin\jar.exe" tf engine/EraEngine.jar | Select-String "CreditsScene"
```

Wenn `CreditsScene.class` enthalten ist, ist die Scene im JAR vorhanden.

**Schritt 4 – Prüfen, ob die Credits-Scene geladen wird:**  
Im Output beim Spielstart sollte Folgendes erscheinen:

```
[SceneManager] Switched to: main_menu
[SceneManager] Switched to: credits
[SceneManager] Switched to: main_menu
```

Wenn diese Zeilen im Log erscheinen (z. B. wenn man im Spiel auf Credits klickt oder der `--help`-Modus durchläuft), dann funktioniert die Scene-Verknüpfung grundsätzlich.

---

### 2. `EraEngine.jar` wird nicht gefunden

**Symptom:**  
Beim Start mit `launcher.bat` erscheint:

```
[FEHLER] EraEngine.jar nicht gefunden.
```

**Ursache:**  
Die Datei `engine/EraEngine.jar` existiert nicht. Das passiert, wenn noch nie ein Build durchgeführt wurde oder der `dist`-Task fehlgeschlagen ist.

**Lösung:**

```
gradlew clean build
```

Danach sollte `engine/EraEngine.jar` vorhanden sein.

**Prüfung:**

```
Test-Path engine/EraEngine.jar
# → True
```

---

### 3. Gradle erkennt Quellcode-Änderungen nicht (`UP-TO-DATE`)

**Symptom:**  
Nach dem Ändern einer `.java`-Datei sagt Gradle beim Build:

```
> Task :compileJava UP-TO-DATE
```

Das bedeutet, Gradle denkt, der Code habe sich seit dem letzten Build nicht geändert, und überspringt die Kompilierung.

**Ursache:**  
Gradle verwendet **incrementale Builds**: Es speichert einen Cache von Datei-Hashes und Task-Eingaben. Wenn sich der Zeitstempel oder Inhalt einer Quelldatei geändert hat, sollte Gradle das normalerweise erkennen. In seltenen Fällen (z. B. nach einem `git checkout`, nach Systemzeit-Umstellungen oder wenn Dateien über externe Tools bearbeitet wurden) kann der Cache inkonsistent werden.

**Lösung:**  
Den gesamten Build-Cache löschen und neu bauen:

```
gradlew clean build
```

- `clean` löscht `build/` inklusive aller Caches.
- `build` erstellt alles neu – alle Tasks laufen von Grund auf.

**Alternativ (nur die Kompilierung erzwingen):**

```
gradlew clean compileJava
```

Das kompiliert nur, ohne JAR zu packen – nützlich zum schnellen Testen, ob der Code fehlerfrei ist.

---

### 4. Vollständige Beispiel-Walkthrough

Angenommen, du hast `MainMenuScene.java` geändert und der Credits-Button fehlt – hier ist der komplette Ablauf zur Fehlerbehebung:

**Schritt 1 – Änderung prüfen:**

```
git diff HEAD -- src/main/java/com/eraoftribes/game/scene/MainMenuScene.java
```

**Schritt 2 – Alte Build-Artefakte löschen und neu bauen:**

```
gradlew clean build
```

**Schritt 3 – Prüfen, ob das JAR in engine/ aktualisiert wurde:**

```
Get-Item engine/EraEngine.jar | Select-Object LastWriteTime, Length
```

Der Zeitstempel sollte jetzt der aktuelle sein.

**Schritt 4 – Spiel starten:**

```
launcher.bat
```

**Schritt 5 – Im Hauptmenü prüfen, ob "Credits" als vierter Button erscheint.**

Falls nicht:

**Schritt 6 – Log prüfen** (wenn `--log logs/` übergeben wurde):

```
Get-Content logs\launch.log
```

Nach Zeilen mit `[SceneManager]` oder `[ERROR]` suchen.

**Schritt 7 – Manuelles Kopieren versuchen, falls der dist-Task nicht funktioniert:**

```
Copy-Item build/libs/EraOfTribes-1.0.0.jar engine/EraEngine.jar -Force
```

Dann wieder `launcher.bat` starten.

---

*Black Beacon Games*
