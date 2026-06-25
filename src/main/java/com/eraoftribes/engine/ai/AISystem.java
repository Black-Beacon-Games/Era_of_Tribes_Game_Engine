package com.eraoftribes.engine.ai;

import java.util.Random;

public class AISystem {
    private final Random random = new Random();

    public AISystem() {
        System.out.println("[AISystem] Initialized.");
    }

    public AIDecision decide(AIContext context) {
        var decision = new AIDecision();
        decision.action = AIDecision.Action.values()[random.nextInt(AIDecision.Action.values().length)];
        return decision;
    }

    public static class AIContext {
        public int gold, population, militaryStrength;
        public int enemyStrength;
        public String[] availableTechnologies;
        public String[] availableBuildings;
    }

    public static class AIDecision {
        public enum Action { BUILD, RESEARCH, ATTACK, TRADE, DIPLOMACY, SETTLE, WAIT }
        public Action action;
        public String target;
        public int priority;
    }
}
