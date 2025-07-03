package com.rpgen.chrono.entity;

import java.util.Map;

public class ChronoEquipment {
    private String name;
    private String type; // weapon, armor, helmet, accessory
    private Map<String, Integer> statBonuses; // Ej: {"attack": 10, "defense": 5}
    private String effect; // Efecto especial, si aplica

    public ChronoEquipment(String name, String type, Map<String, Integer> statBonuses, String effect) {
        this.name = name;
        this.type = type;
        this.statBonuses = statBonuses;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, Integer> getStatBonuses() {
        return statBonuses;
    }

    public String getEffect() {
        return effect;
    }
} 