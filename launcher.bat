@echo off
REM Era of Tribes - Launcher (Wrapper)
REM Startet die EraEngine mit dem geladenen Spiel.

set ENGINE_DIR=%~dp0engine
set GAME_DIR=%~dp0game
set LOG_DIR=%~dp0logs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo ============================================
echo  Era of Tribes v1.0.0
echo ============================================
echo.

if not exist "%ENGINE_DIR%\EraEngine.jar" (
    echo [FEHLER] EraEngine.jar nicht gefunden.
    pause
    exit /b 1
)

echo  Starte Era of Tribes...
echo  Engine: %ENGINE_DIR%\EraEngine.jar
echo  Spiel:  %GAME_DIR%
echo  Log:    %LOG_DIR%\launch.log
echo.
java -jar "%ENGINE_DIR%\EraEngine.jar" --game "%GAME_DIR%" --log "%LOG_DIR%"
echo.
echo  Engine beendet.
pause
