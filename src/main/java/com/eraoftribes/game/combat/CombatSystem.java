package com.eraoftribes.game.combat;

import com.eraoftribes.game.unit.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CombatSystem {
    private final Random random = new Random();
    private final List<Battle> battleHistory = new ArrayList<>();

    public CombatSystem() {
        System.out.println("[CombatSystem] Initialized.");
    }

    public Battle resolve(String attackerId, String defenderId,
                          List<Unit> attackerUnits, List<Unit> defenderUnits) {
        var battle = new Battle(attackerId, defenderId);
        var atk = new ArrayList<>(attackerUnits);
        var def = new ArrayList<>(defenderUnits);

        int round = 0;
        while (!atk.isEmpty() && !def.isEmpty() && round < 20) {
            round++;
            resolveRound(atk, def);
            if (checkMorale(atk) < 15) break;
            if (checkMorale(def) < 15) break;
        }

        battle.attackerSurvivors = new ArrayList<>(atk);
        battle.defenderSurvivors = new ArrayList<>(def);
        battle.rounds = round;

        if (def.isEmpty()) battle.winner = attackerId;
        else if (atk.isEmpty()) battle.winner = defenderId;
        else battle.winner = "draw";

        battleHistory.add(battle);
        System.out.println("[Combat] Battle resolved: " + battle.winner + " wins (" + round + " rounds)");
        return battle;
    }

    private void resolveRound(List<Unit> attackers, List<Unit> defenders) {
        for (var unit : List.copyOf(attackers)) {
            if (defenders.isEmpty()) break;
            var target = defenders.get(random.nextInt(defenders.size()));
            int dmg = calculateDamage(unit, target);
            target.takeDamage(dmg);
            if (!target.isAlive()) defenders.remove(target);
        }

        for (var unit : List.copyOf(defenders)) {
            if (attackers.isEmpty()) break;
            var target = attackers.get(random.nextInt(attackers.size()));
            int dmg = calculateDamage(unit, target);
            target.takeDamage(dmg);
            if (!target.isAlive()) attackers.remove(target);
        }
    }

    private int calculateDamage(Unit attacker, Unit defender) {
        double base = attacker.attack;
        double reduction = (double) defender.defense / (defender.defense + 10);
        return Math.max(1, (int) (base * (1 - reduction) * (0.8 + random.nextDouble() * 0.4)));
    }

    private int checkMorale(List<Unit> units) {
        if (units.isEmpty()) return 0;
        return (int) units.stream().mapToInt(u -> u.morale).average().orElse(0);
    }

    public List<Battle> getBattleHistory() { return battleHistory; }

    public static class Battle {
        public final String attackerId;
        public final String defenderId;
        public String winner;
        public int rounds;
        public List<Unit> attackerSurvivors;
        public List<Unit> defenderSurvivors;

        Battle(String a, String d) {
            this.attackerId = a;
            this.defenderId = d;
        }
    }
}
