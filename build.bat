@echo off
REM Era of Tribes - Build-Tool
REM Fuehrt den Gradle-Build aus und kopiert das Ergebnis.

setlocal enabledelayedexpansion

echo ============================================
echo  Era of Tribes Build Tool v1.0.0
echo ============================================
echo.

if not exist "gradlew.bat" (
    echo [FEHLER] Gradle-Wrapper nicht gefunden.
    echo Bitte stelle sicher, dass gradlew.bat im Projektstamm existiert.
    pause
    exit /b 1
)

echo [1/7] Projekt validieren...
if exist "game\" (echo  OK - game/-Struktur vorhanden) else (echo  WARN - Kein game/-Verzeichnis)
if exist "src\" (echo  OK - Java-Quellcode gefunden) else (echo  FEHLER - Kein src/-Verzeichnis & pause & exit /b 1)

echo [2/7] Abhaengigkeiten aufloesen...
call gradlew.bat --no-daemon dependencies 2>&1
if %ERRORLEVEL% neq 0 (
    echo [FEHLER] Abhaengigkeiten konnten nicht aufgeloest werden.
    pause
    exit /b 1
)
echo  OK - Abhaengigkeiten geladen.

echo [3/7] Java-Quellcode kompilieren...
call gradlew.bat --no-daemon compileJava 2>&1
if %ERRORLEVEL% neq 0 (
    echo [FEHLER] Kompilierung fehlgeschlagen.
    pause
    exit /b 1
)
echo  OK - Quellcode kompiliert.

echo [4/7] Tests ausfuehren...
call gradlew.bat --no-daemon test 2>&1
if %ERRORLEVEL% neq 0 (
    echo [WARNUNG] Tests fehlgeschlagen.
) else (
    echo  OK - Tests bestanden.
)

echo [5/7] JAR-Paket erstellen...
call gradlew.bat --no-daemon jar 2>&1
if %ERRORLEVEL% neq 0 (
    echo [FEHLER] JAR-Erstellung fehlgeschlagen.
    pause
    exit /b 1
)
echo  OK - JAR erstellt.

echo [6/7] JAR nach engine/EraEngine.jar kopieren...
call gradlew.bat --no-daemon dist 2>&1
if %ERRORLEVEL% neq 0 (
    echo [FEHLER] Kopieren fehlgeschlagen.
    pause
    exit /b 1
)
echo  OK - engine/EraEngine.jar aktualisiert.

echo [7/7] Build-Report schreiben...
echo  Build abgeschlossen am %DATE% um %TIME% > logs\build.log
echo  Projekt: Era of Tribes >> logs\build.log
echo  Status:  Erfolgreich >> logs\build.log

echo.
echo ============================================
echo  Build erfolgreich abgeschlossen!
echo ============================================
echo.
echo  JAR:      engine\EraEngine.jar
echo  Log:      logs\build.log
echo.
echo  Starten:  launcher.exe
echo  oder:     gradlew.bat runGame
echo ============================================

exit /b 0
