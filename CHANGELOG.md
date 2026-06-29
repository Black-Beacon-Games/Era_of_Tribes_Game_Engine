# Changelog

## v0.0.3 (2026-06-26)

### Neu
- **Debug-Overlay**: Anzeige von FPS, Frametime, Speicher, Version und Maus-Koordinaten
  über den Debug-Tab in den Einstellungen aktivierbar
- **Debug-Konsole**: Eigenes Konsolen-Fenster mit Befehlen (`help`, `clear`, `gc`,
  `mem`, `threads`, `exit`) – über den "Console"-Toggle in den Einstellungen
- **Einstellungen speichern**: Alle Änderungen in den Settings werden sofort in
  `engine/config/engine.json` persistiert und beim Beenden nochmal geschrieben
- **Max FPS einstellbar**: Target FPS (30/60/120/144/240/Unlimited) im Graphics-Tab

### Verbessert
- **Game-Loop überarbeitet**: Kein Frame-Skipping mehr. Echte Delta-Zeit statt
  festem 0.016s. FPS-Capping erst nach dem Rendern (kein `Thread.sleep(1)`-
  Spin-Wait mehr, der auf Windows effektiv ~33 FPS erzwungen hat)
- **Absturzsicherheit**: Alle Config-Klassen haben jetzt Default-Werte, sodass
  das Spiel auch bei fehlerhafter `engine.json` startet

### Behoben
- Absturz nach dem Ladebildschirm durch NullPointerException in der Game-Loop,
  wenn Config-Unterobjekte (`renderer`, `debug`, etc.) `null` waren
- Performance-Einbruch durch `Thread.sleep(1)` auf Windows (~33 FPS statt 60+)
- Einstellungen gingen nach Neustart verloren (kein Speichern auf Disk)
- **Fenster-schließen per X**: `running`-Flag ist jetzt `volatile`, sodass der
  Klick auf X sofort vom Game-Loop erkannt wird
- **Quit-Button**: Ruft jetzt `engine.requestShutdown()` auf statt
  `System.exit(0)` – speichert Config und fährt sauber herunter
- **Langsames Beenden**: Audio-Fadeout (300ms) in `stopAll()` übersprungen,
  Musik-Thread wird direkt geschlossen
- **Träge Buttons**: Maus-Input-Felder (`mouseX`, `mouseY`, `mouseClicked`,
  `mousePressed`, `lastKey`) sind jetzt `volatile` – die Hauptschleife sieht
  Klicks sofort, ohne Verzögerung
- **Default Target FPS**: Von 60 auf **144 FPS** erhöht (smooth auch auf
  High-Refresh-Monitoren)
- **Exit-Button repariert**: `requestShutdown()` ruft jetzt direkt `shutdown()`
  plus `System.exit(0)` auf, statt nur `running = false` zu setzen und auf den
  Game-Loop zu warten. `shutdown()` hat einen `shutdownDone`-Guard gegen
  doppeltes Ausführen
- **Fenstergröße/Borderless/Fullscreen sofort übernommen**: Graphics-Tab-Änderungen
  rufen `Renderer.applyConfig()` auf. Resolution wird sofort via `frame.setSize()`
  angepasst, Borderless/Fullscreen-Wechsel lösen einen `pendingRebuild` aus, der
  zu Beginn des nächsten Frames das Fenster neu aufbaut (`doBuildWindow()`)
  – keine Verzögerung bis zum Neustart mehr
