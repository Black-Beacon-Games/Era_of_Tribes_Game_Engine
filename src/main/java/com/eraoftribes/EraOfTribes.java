package com.eraoftribes;

import com.eraoftribes.engine.Engine;
import com.eraoftribes.game.Game;

public class EraOfTribes {
    public static void main(String[] args) {
        Engine engine = new Engine(args);
        engine.init();
        Game game = new Game(engine);
        game.init();
        engine.start(game);
    }
}
