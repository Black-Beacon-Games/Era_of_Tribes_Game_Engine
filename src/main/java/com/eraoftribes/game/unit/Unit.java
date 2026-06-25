package com.eraoftribes.game.unit;

import java.util.Map;

public class Unit {
    private final String id;
    public final String name;
    public final String type;
    public final Map<String, Integer> cost;
    public int hp;
    public final int maxHp;
    public final int attack;
    public final int defense;
    public final int moveRange;
    public final int attackRange;
    public final String special;
    public int experience;
    public int morale;
    public int x, y;

    public Unit(String id, String name, String type, Map<String, Integer> cost,
                int hp, int attack, int defense, int moveRange, int attackRange, String special) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.cost = cost;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
        this.moveRange = moveRange;
        this.attackRange = attackRange;
        this.special = special;
        this.experience = 0;
        this.morale = 50;
    }

    public String getId() { return id; }

    public void takeDamage(int dmg) { hp = Math.max(0, hp - dmg); }
    public boolean isAlive() { return hp > 0; }
    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }
}
