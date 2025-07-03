package com.rpgen.chrono.entity;

import java.util.List;
import java.util.Map;
import com.rpgen.core.entity.Entity;
import com.rpgen.core.action.GameAction;
import java.util.*;

public class ChronoEntity implements Entity {
    // Campos del JSON
    private String character;
    private List<StatByLevel> statsByLevel;

    // Atributos básicos para ATB
    private int speed; // Velocidad determina la rapidez del ATB
    private int atbCounter;
    private int atbMax;
    private boolean canAct;

    // --- NUEVO: Estado actual de la entidad ---
    private int hp;
    private int mp;
    private ChronoStatus status;

    // --- NUEVO: Equipo equipado ---
    private ChronoEquipment weapon;
    private ChronoEquipment armor;
    private ChronoEquipment helmet;
    private ChronoEquipment accessory;
    
    // --- NUEVO: Efectos de equipamiento ---
    private Map<String, Object> equipmentEffects;

    private int level;
    private List<GameAction> availableActions = new ArrayList<>();
    private int defenseBonus = 0;

    public ChronoEntity() {
        this.atbCounter = 0;
        this.atbMax = 1000;
        this.canAct = false;
    }

    // Inicializa el ATB speed con el valor de Speed del primer nivel
    public void initializeATBFromStats() {
        if (statsByLevel != null && !statsByLevel.isEmpty()) {
            this.speed = statsByLevel.get(0).Speed;
        }
    }

    public void advanceATB() {
        if (!canAct) {
            atbCounter += speed;
            if (atbCounter >= atbMax) {
                atbCounter = atbMax;
                canAct = true;
            }
        }
    }

    public boolean isATBFull() {
        return canAct;
    }

    public boolean canAct() {
        return canAct;
    }

    public void resetATB() {
        atbCounter = 0;
        canAct = false;
    }

    // Getters y setters
    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }
    public List<StatByLevel> getStatsByLevel() { return statsByLevel; }
    public void setStatsByLevel(List<StatByLevel> statsByLevel) { this.statsByLevel = statsByLevel; }
    public void setSpeed(int speed) { this.speed = speed; }
    public int getAtbCounter() { return atbCounter; }
    public int getAtbMax() { return atbMax; }
    public void setAtbMax(int atbMax) { this.atbMax = atbMax; }

    // --- NUEVO: Getters y setters para HP, MP y status ---
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMp() { return mp; }
    public void setMp(int mp) { this.mp = mp; }
    public ChronoStatus getStatus() { return status; }
    public void setStatus(ChronoStatus status) { this.status = status; }

    public void setAtbCounter(int atbCounter) { this.atbCounter = atbCounter; }
    public void setCanAct(boolean canAct) { this.canAct = canAct; }

    // Métodos para equipar y obtener equipo
    public void equipWeapon(ChronoEquipment weapon) { this.weapon = weapon; }
    public void equipArmor(ChronoEquipment armor) { this.armor = armor; }
    public void equipHelmet(ChronoEquipment helmet) { this.helmet = helmet; }
    public void equipAccessory(ChronoEquipment accessory) { this.accessory = accessory; }
    public ChronoEquipment getWeapon() { return weapon; }
    public ChronoEquipment getArmor() { return armor; }
    public ChronoEquipment getHelmet() { return helmet; }
    public ChronoEquipment getAccessory() { return accessory; }
    
    // Getters y setters para efectos de equipamiento
    public Map<String, Object> getEquipmentEffects() { return equipmentEffects; }
    public void setEquipmentEffects(Map<String, Object> equipmentEffects) { this.equipmentEffects = equipmentEffects; }

    // Devuelve el bono total de una estadística sumando el equipo
    public int getTotalBonus(String stat) {
        int bonus = 0;
        if (weapon != null && weapon.getStatBonuses() != null) bonus += weapon.getStatBonuses().getOrDefault(stat, 0);
        if (armor != null && armor.getStatBonuses() != null) bonus += armor.getStatBonuses().getOrDefault(stat, 0);
        if (helmet != null && helmet.getStatBonuses() != null) bonus += helmet.getStatBonuses().getOrDefault(stat, 0);
        if (accessory != null && accessory.getStatBonuses() != null) bonus += accessory.getStatBonuses().getOrDefault(stat, 0);
        return bonus;
    }

    // Devuelve los stats del nivel actual
    public StatByLevel getStatsForCurrentLevel() {
        if (statsByLevel == null || statsByLevel.isEmpty()) return null;
        for (StatByLevel s : statsByLevel) {
            if (s.getLevel() == this.level) return s;
        }
        // Si no se encuentra, devuelve el primer nivel
        return statsByLevel.get(0);
    }

    // Clase interna para stats por nivel
    public static class StatByLevel {
        private int level;
        private int HP;
        private int MP;
        private int Strength;
        private int Stamina;
        private int Accuracy;
        private int Evasion;
        private int Magic;
        private int MagicDefense;
        private int Speed;

        // Getters y setters
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public int getHP() { return HP; }
        public void setHP(int HP) { this.HP = HP; }
        public int getMP() { return MP; }
        public void setMP(int MP) { this.MP = MP; }
        public int getStrength() { return Strength; }
        public void setStrength(int Strength) { this.Strength = Strength; }
        public int getStamina() { return Stamina; }
        public void setStamina(int Stamina) { this.Stamina = Stamina; }
        public int getAccuracy() { return Accuracy; }
        public void setAccuracy(int Accuracy) { this.Accuracy = Accuracy; }
        public int getEvasion() { return Evasion; }
        public void setEvasion(int Evasion) { this.Evasion = Evasion; }
        public int getMagic() { return Magic; }
        public void setMagic(int Magic) { this.Magic = Magic; }
        public int getMagicDefense() { return MagicDefense; }
        public void setMagicDefense(int MagicDefense) { this.MagicDefense = MagicDefense; }
        public int getSpeed() { return Speed; }
        public void setSpeed(int Speed) { this.Speed = Speed; }
    }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    @Override
    public String getId() { return character != null ? character : ""; }
    @Override
    public String getName() { return character != null ? character : ""; }
    @Override
    public int getHealth() { return hp; }
    @Override
    public int getMaxHealth() {
        StatByLevel stats = getStatsForCurrentLevel();
        return stats != null ? stats.getHP() : 1;
    }
    @Override
    public int getAttack() {
        StatByLevel stats = getStatsForCurrentLevel();
        int base = stats != null ? stats.getStrength() : 0;
        return base + getTotalBonus("attack");
    }
    @Override
    public int getDefense() {
        StatByLevel stats = getStatsForCurrentLevel();
        int base = stats != null ? stats.getStamina() : 0;
        return base + getTotalBonus("defense") + defenseBonus;
    }
    @Override
    public int getSpeed() {
        StatByLevel stats = getStatsForCurrentLevel();
        return stats != null ? stats.getSpeed() : speed;
    }
    @Override
    public boolean isAlive() { return hp > 0; }
    @Override
    public void takeDamage(int damage) { hp = Math.max(0, hp - damage); }
    @Override
    public void heal(int amount) {
        int maxHp = getMaxHealth();
        hp = Math.min(hp + amount, maxHp);
    }
    @Override
    public void setDefense(int bonus) { this.defenseBonus = bonus; }
    @Override
    public List<GameAction> getAvailableActions() { return new ArrayList<>(availableActions); }
    @Override
    public void setAvailableActions(List<GameAction> actions) { this.availableActions = new ArrayList<>(actions); }
    @Override
    public boolean isDefeated() { return hp <= 0; }
} 