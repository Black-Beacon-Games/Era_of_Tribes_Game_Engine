package com.eraoftribes.game.scene;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.engine.rendering.Renderer;
import com.eraoftribes.engine.scene.Scene;
import com.eraoftribes.game.Game;
import java.util.Scanner;

public class MainMenuScene extends Scene {
    private final Engine engine;
    private final Game game;
    private final Scanner scanner;
    private boolean waiting;

    public MainMenuScene(Engine engine, Game game) {
        super("main_menu");
        this.engine = engine;
        this.game = game;
        this.scanner = new Scanner(System.in);
        this.waiting = false;
    }

    public void onEnter() {
        waiting = true;
        draw();
        listenInput();
    }

    public void onLeave() {
        waiting = false;
    }

    public void update(double dt) {}

    public void render(Renderer renderer) {}

    private void draw() {
        clearScreen();
        System.out.println();
        System.out.println("  ============================================");
        System.out.println("         E R A   O F   T R I B E S");
        System.out.println("  ============================================");
        System.out.println("             v1.0.0");
        System.out.println();
        System.out.println("  [1] Play");
        System.out.println("  [2] Settings");
        System.out.println("  [3] Quit");
        System.out.println();
        System.out.print("  Select an option: ");
    }

    private void listenInput() {
        while (waiting && scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!waiting) return;
            switch (line) {
                case "1" -> {
                    waiting = false;
                    engine.getSceneManager().switchTo("game");
                    return;
                }
                case "2" -> {
                    waiting = false;
                    engine.getSceneManager().switchTo("settings");
                    return;
                }
                case "3" -> {
                    System.out.println("  Goodbye!");
                    waiting = false;
                    System.exit(0);
                    return;
                }
                default -> {
                    System.out.print("  Invalid option. Try again: ");
                }
            }
        }
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
