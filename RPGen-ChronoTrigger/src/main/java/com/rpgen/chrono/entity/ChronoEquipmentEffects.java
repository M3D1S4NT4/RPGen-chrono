package com.rpgen.chrono.entity;

import java.util.Map;
import java.util.HashMap;

public class ChronoEquipmentEffects {
    
    // Efectos de resistencia a elementos
    public static final String FIRE_RESISTANCE = "fire_resistance";
    public static final String WATER_RESISTANCE = "water_resistance";
    public static final String LIGHTNING_RESISTANCE = "lightning_resistance";
    public static final String SHADOW_RESISTANCE = "shadow_resistance";
    public static final String PHYSICAL_RESISTANCE = "physical_resistance";
    public static final String MAGIC_RESISTANCE = "magic_resistance";
    
    // Efectos de absorción
    public static final String FIRE_ABSORB = "fire_absorb";
    public static final String WATER_ABSORB = "water_absorb";
    public static final String LIGHTNING_ABSORB = "lightning_absorb";
    public static final String SHADOW_ABSORB = "shadow_absorb";
    
    // Efectos de protección de estado
    public static final String STATUS_PROTECTION = "status_protection";
    public static final String CHAOS_PROTECTION = "chaos_protection";
    public static final String LOCK_PROTECTION = "lock_protection";
    public static final String STOP_SLOW_PROTECTION = "stop_slow_protection";
    
    // Efectos especiales
    public static final String COUNTER_ATTACK = "counter_attack";
    public static final String FRENZY_MODE = "frenzy_mode";
    public static final String AUTO_REVIVE = "auto_revive";
    public static final String MP_REDUCTION = "mp_reduction";
    public static final String HP_BOOST = "hp_boost";
    public static final String SPEED_BOOST = "speed_boost";
    
    /**
     * Parsea los efectos de un string de efecto y devuelve un mapa de efectos
     */
    public static Map<String, Object> parseEffects(String effectStr) {
        Map<String, Object> effects = new HashMap<>();
        if (effectStr == null || effectStr.equals("-") || effectStr.trim().isEmpty()) {
            return effects;
        }
        
        String effect = effectStr.toLowerCase();
        
        // Resistencia al fuego
        if (effect.contains("fire damage reduced by 50%")) {
            effects.put(FIRE_RESISTANCE, 0.5);
        } else if (effect.contains("fire damage reduced by 80%")) {
            effects.put(FIRE_RESISTANCE, 0.2);
        } else if (effect.contains("fire damage absorbed by 50%")) {
            effects.put(FIRE_ABSORB, 0.5);
        } else if (effect.contains("absorbs fire magic")) {
            effects.put(FIRE_ABSORB, 1.0);
        }
        
        // Resistencia al agua
        if (effect.contains("water damage absorbed by 50%")) {
            effects.put(WATER_ABSORB, 0.5);
        } else if (effect.contains("absorbs water magic")) {
            effects.put(WATER_ABSORB, 1.0);
        } else if (effect.contains("reduces water damage by 50%")) {
            effects.put(WATER_RESISTANCE, 0.5);
        }
        
        // Resistencia a rayos
        if (effect.contains("lightning damage absorbed by 50%")) {
            effects.put(LIGHTNING_ABSORB, 0.5);
        } else if (effect.contains("absorbs lightning magic")) {
            effects.put(LIGHTNING_ABSORB, 1.0);
        } else if (effect.contains("reduces lightning damage by 50%")) {
            effects.put(LIGHTNING_RESISTANCE, 0.5);
        }
        
        // Resistencia a sombra
        if (effect.contains("shadow damage absorbed by 50%")) {
            effects.put(SHADOW_ABSORB, 0.5);
        } else if (effect.contains("absorbs shadow magic")) {
            effects.put(SHADOW_ABSORB, 1.0);
        } else if (effect.contains("reduces shadow damage by 50%")) {
            effects.put(SHADOW_RESISTANCE, 0.5);
        }
        
        // Resistencia física
        if (effect.contains("cuts physical damage by 1/3")) {
            effects.put(PHYSICAL_RESISTANCE, 0.67);
        }
        
        // Resistencia mágica
        if (effect.contains("cuts magic attacks by 1/3")) {
            effects.put(MAGIC_RESISTANCE, 0.67);
        }
        
        // Protección de estado
        if (effect.contains("protects status")) {
            effects.put(STATUS_PROTECTION, true);
        }
        if (effect.contains("prevents \"chaos\" status effect")) {
            effects.put(CHAOS_PROTECTION, true);
        }
        if (effect.contains("prevents \"lock\" status effect")) {
            effects.put(LOCK_PROTECTION, true);
        }
        if (effect.contains("prevents \"stop\" and \"slow\" status effects")) {
            effects.put(STOP_SLOW_PROTECTION, true);
        }
        
        // Efectos especiales
        if (effect.contains("counterattacks with 50% chance")) {
            effects.put(COUNTER_ATTACK, 0.5);
        }
        if (effect.contains("attack up 80%, but only attack")) {
            effects.put(FRENZY_MODE, 1.8);
        }
        if (effect.contains("1-time auto-revive")) {
            effects.put(AUTO_REVIVE, true);
        }
        if (effect.contains("mp use cut by 50%")) {
            effects.put(MP_REDUCTION, 0.5);
        } else if (effect.contains("mp use cut by 75%")) {
            effects.put(MP_REDUCTION, 0.25);
        }
        if (effect.contains("max hp up 25%")) {
            effects.put(HP_BOOST, 1.25);
        } else if (effect.contains("max hp up 50%")) {
            effects.put(HP_BOOST, 1.5);
        }
        if (effect.contains("increases speed by 50%")) {
            effects.put(SPEED_BOOST, 1.5);
        }
        
        return effects;
    }
    
    /**
     * Aplica los efectos de equipamiento a una entidad
     */
    public static void applyEquipmentEffects(ChronoEntity entity) {
        Map<String, Object> totalEffects = new HashMap<>();
        
        // Recolectar efectos de todo el equipo
        if (entity.getWeapon() != null && entity.getWeapon().getEffect() != null) {
            totalEffects.putAll(parseEffects(entity.getWeapon().getEffect()));
        }
        if (entity.getArmor() != null && entity.getArmor().getEffect() != null) {
            totalEffects.putAll(parseEffects(entity.getArmor().getEffect()));
        }
        if (entity.getHelmet() != null && entity.getHelmet().getEffect() != null) {
            totalEffects.putAll(parseEffects(entity.getHelmet().getEffect()));
        }
        if (entity.getAccessory() != null && entity.getAccessory().getEffect() != null) {
            totalEffects.putAll(parseEffects(entity.getAccessory().getEffect()));
        }
        
        // Aplicar efectos de HP boost
        if (totalEffects.containsKey(HP_BOOST)) {
            double hpMultiplier = (Double) totalEffects.get(HP_BOOST);
            if (entity.getStatsByLevel() != null && !entity.getStatsByLevel().isEmpty()) {
                int baseHP = entity.getStatsByLevel().get(0).getHP();
                int boostedHP = (int) (baseHP * hpMultiplier);
                entity.setHp(boostedHP);
            }
        }
        
        // Aplicar efectos de velocidad
        if (totalEffects.containsKey(SPEED_BOOST)) {
            double speedMultiplier = (Double) totalEffects.get(SPEED_BOOST);
            int baseSpeed = entity.getSpeed();
            entity.setSpeed((int) (baseSpeed * speedMultiplier));
        }
        
        // Guardar los efectos en la entidad para uso posterior
        entity.setEquipmentEffects(totalEffects);
    }
    
    /**
     * Calcula el modificador de daño basado en las resistencias del equipo
     */
    public static double calculateDamageModifier(ChronoEntity target, String damageType) {
        Map<String, Object> effects = target.getEquipmentEffects();
        if (effects == null) return 1.0;
        
        switch (damageType.toLowerCase()) {
            case "fire":
                if (effects.containsKey(FIRE_ABSORB)) {
                    return 0.0; // Absorbe completamente
                } else if (effects.containsKey(FIRE_RESISTANCE)) {
                    return (Double) effects.get(FIRE_RESISTANCE);
                }
                break;
            case "water":
                if (effects.containsKey(WATER_ABSORB)) {
                    return 0.0;
                } else if (effects.containsKey(WATER_RESISTANCE)) {
                    return (Double) effects.get(WATER_RESISTANCE);
                }
                break;
            case "lightning":
                if (effects.containsKey(LIGHTNING_ABSORB)) {
                    return 0.0;
                } else if (effects.containsKey(LIGHTNING_RESISTANCE)) {
                    return (Double) effects.get(LIGHTNING_RESISTANCE);
                }
                break;
            case "shadow":
                if (effects.containsKey(SHADOW_ABSORB)) {
                    return 0.0;
                } else if (effects.containsKey(SHADOW_RESISTANCE)) {
                    return (Double) effects.get(SHADOW_RESISTANCE);
                }
                break;
            case "physical":
                if (effects.containsKey(PHYSICAL_RESISTANCE)) {
                    return (Double) effects.get(PHYSICAL_RESISTANCE);
                }
                break;
            case "magic":
                if (effects.containsKey(MAGIC_RESISTANCE)) {
                    return (Double) effects.get(MAGIC_RESISTANCE);
                }
                break;
        }
        
        return 1.0;
    }
    
    /**
     * Verifica si una entidad está protegida contra un estado específico
     */
    public static boolean isProtectedFromStatus(ChronoEntity entity, String statusName) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        if (effects == null) return false;
        
        switch (statusName.toLowerCase()) {
            case "chaos":
                return effects.containsKey(CHAOS_PROTECTION);
            case "lock":
                return effects.containsKey(LOCK_PROTECTION);
            case "stop":
            case "slow":
                return effects.containsKey(STOP_SLOW_PROTECTION);
            default:
                return effects.containsKey(STATUS_PROTECTION);
        }
    }
    
    /**
     * Calcula el costo de MP modificado por equipamiento
     */
    public static int calculateModifiedMPCost(ChronoEntity entity, int baseCost) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        if (effects == null || !effects.containsKey(MP_REDUCTION)) {
            return baseCost;
        }
        
        double reduction = (Double) effects.get(MP_REDUCTION);
        return (int) (baseCost * reduction);
    }
    
    /**
     * Verifica si una entidad tiene el efecto de contraataque
     */
    public static boolean hasCounterAttack(ChronoEntity entity) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        if (effects == null) return false;
        
        if (effects.containsKey(COUNTER_ATTACK)) {
            double chance = (Double) effects.get(COUNTER_ATTACK);
            return Math.random() < chance;
        }
        return false;
    }
    
    /**
     * Verifica si una entidad está en modo frenesí
     */
    public static boolean isInFrenzyMode(ChronoEntity entity) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        return effects != null && effects.containsKey(FRENZY_MODE);
    }
    
    /**
     * Obtiene el multiplicador de ataque en modo frenesí
     */
    public static double getFrenzyAttackMultiplier(ChronoEntity entity) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        if (effects != null && effects.containsKey(FRENZY_MODE)) {
            return (Double) effects.get(FRENZY_MODE);
        }
        return 1.0;
    }
    
    /**
     * Verifica si una entidad tiene auto-revive
     */
    public static boolean hasAutoRevive(ChronoEntity entity) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        return effects != null && effects.containsKey(AUTO_REVIVE);
    }
    
    /**
     * Consume el auto-revive de una entidad
     */
    public static void consumeAutoRevive(ChronoEntity entity) {
        Map<String, Object> effects = entity.getEquipmentEffects();
        if (effects != null && effects.containsKey(AUTO_REVIVE)) {
            effects.remove(AUTO_REVIVE);
        }
    }
} 