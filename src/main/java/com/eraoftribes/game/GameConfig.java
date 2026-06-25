package com.eraoftribes.game;

public class GameConfig {
    public int maxTurns = 200;
    public int maxPlayers = 8;
    public int startGold = 100;
    public int startPopulation = 20;
    public double baseTaxRate = 0.3;
    public boolean fogOfWar = true;
    public boolean allowAlliances = true;
    public boolean allowTrade = true;

    public VictoryCondition victoryCondition = VictoryCondition.CONQUEST;

    public enum VictoryCondition {
        CONQUEST,
        CULTURE,
        TECHNOLOGY,
        POINTS,
        LAST_STANDING
    }
}
