package com.eraoftribes.game.diplomacy;

import java.util.HashMap;
import java.util.Map;

public class DiplomacySystem {
    private final Map<String, DiplomacyStatus> relations = new HashMap<>();

    public DiplomacySystem() {
        System.out.println("[DiplomacySystem] Initialized.");
    }

    public DiplomacyStatus getRelation(String tribeA, String tribeB) {
        var key = getKey(tribeA, tribeB);
        return relations.computeIfAbsent(key, k -> new DiplomacyStatus());
    }

    public void declareWar(String from, String target) {
        var rel = getRelation(from, target);
        rel.treaty = null;
        rel.opinion = -100;
        System.out.println("[Diplomacy] War declared: " + from + " -> " + target);
    }

    public void makePeace(String tribeA, String tribeB) {
        var rel = getRelation(tribeA, tribeB);
        rel.treaty = "peace";
        rel.opinion = Math.min(rel.opinion + 30, 50);
        System.out.println("[Diplomacy] Peace: " + tribeA + " <-> " + tribeB);
    }

    public void formAlliance(String tribeA, String tribeB) {
        var rel = getRelation(tribeA, tribeB);
        rel.treaty = "alliance";
        rel.opinion = Math.min(rel.opinion + 50, 100);
    }

    public void signTradeAgreement(String tribeA, String tribeB) {
        var rel = getRelation(tribeA, tribeB);
        rel.treaty = "trade";
        rel.opinion = Math.min(rel.opinion + 20, 80);
    }

    public boolean isAtWar(String a, String b) {
        var rel = getRelation(a, b);
        return rel.opinion <= -80 || rel.treaty == null;
    }

    public boolean isAllied(String a, String b) {
        var rel = getRelation(a, b);
        return "alliance".equals(rel.treaty);
    }

    private String getKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
    }

    public static class DiplomacyStatus {
        public int opinion;
        public int trust;
        public String treaty;

        DiplomacyStatus() {
            this.opinion = 0;
            this.trust = 50;
            this.treaty = null;
        }
    }
}
