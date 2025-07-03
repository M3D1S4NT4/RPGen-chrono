package com.rpgen.chrono.battle;

import com.rpgen.core.battle.BattleEngine;
import com.rpgen.core.battle.BattleListener;
import com.rpgen.chrono.entity.ChronoEntity;
import com.rpgen.chrono.entity.ChronoEquipmentEffects;
import com.rpgen.chrono.entity.ChronoMove;
import java.util.*;

public class ChronoBattleEngine extends com.rpgen.core.battle.BattleEngine<ChronoEntity, ChronoMove> {
    // --- Variables de batalla ---
    private List<ChronoEntity> allies;
    private List<ChronoEntity> enemies;
    private List<BattleListener> listeners = new ArrayList<>();
    private Timer atbTimer;
    private boolean battleActive;
    private static final int ATB_TICK_MS = 100;
    private List<String> messages = new ArrayList<>();
    private Map<ChronoEntity, Integer> enemyTurnCounters = new HashMap<>();
    private Map<ChronoEntity, String> enemySpecialAttacks = new HashMap<>();
    // Para gestión de acciones pendientes (como en BattleEngine)
    private List<ActionTuple> pendingActions = new ArrayList<>();

    private static class ActionTuple {
        ChronoEntity actor;
        ChronoMove move;
        ChronoEntity target;
        ActionTuple(ChronoEntity a, ChronoMove m, ChronoEntity t) {
            actor = a; move = m; target = t;
        }
    }

    public ChronoBattleEngine() {
        this.allies = new ArrayList<>();
        this.enemies = new ArrayList<>();
    }

    @Override
    public void initialize(List<ChronoEntity> team1, List<ChronoEntity> team2) {
        this.allies = new ArrayList<>(team1);
        this.enemies = new ArrayList<>(team2);
        this.messages.clear();
        this.enemyTurnCounters.clear();
        this.enemySpecialAttacks.clear();
        this.pendingActions.clear();
        this.battleActive = false;
        if (atbTimer != null) atbTimer.cancel();
        for (ChronoEntity entity : getAllEntities()) {
            entity.resetATB();
            entity.setAtbCounter(entity.getAtbMax());
            entity.setCanAct(true);
            ChronoEquipmentEffects.applyEquipmentEffects(entity);
        }
        for (ChronoEntity enemy : enemies) {
            enemyTurnCounters.put(enemy, 0);
            loadEnemySpecialAttack(enemy);
        }
    }

    @Override
    public void addAction(ChronoEntity actor, ChronoEntity target, ChronoMove action) {
        pendingActions.add(new ActionTuple(actor, action, target));
    }

    @Override
    public void processTurn() {
        if (!battleActive) return;
        if (pendingActions.isEmpty()) return;
        for (ActionTuple tuple : pendingActions) {
            performAction(tuple.actor, tuple.move, tuple.target);
        }
        pendingActions.clear();
    }

    @Override
    public List<ChronoEntity> getActiveEntities() {
        List<ChronoEntity> actives = new ArrayList<>();
        for (ChronoEntity e : getAllEntities()) {
            if (e.canAct() && e.getHp() > 0) actives.add(e);
        }
        return actives;
    }

    @Override
    public boolean isBattleOver() {
        return allies.stream().allMatch(a -> a.getHp() <= 0) || enemies.stream().allMatch(e -> e.getHp() <= 0);
    }

    @Override
    public void registerBattleListener(BattleListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeBattleListener(BattleListener listener) {
        listeners.remove(listener);
    }

    @Override
    public List<ChronoEntity> getTeam1() { return allies; }
    @Override
    public List<ChronoEntity> getTeam2() { return enemies; }

    public void startBattle() {
        if (battleActive) return;
        battleActive = true;
        for (ChronoEntity entity : getAllEntities()) {
            entity.resetATB();
            entity.setAtbCounter(entity.getAtbMax());
            entity.setCanAct(true);
            ChronoEquipmentEffects.applyEquipmentEffects(entity);
        }
        for (ChronoEntity enemy : enemies) {
            enemyTurnCounters.put(enemy, 0);
            loadEnemySpecialAttack(enemy);
        }
        atbTimer = new Timer();
        atbTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { updateATB(); }
        }, 0, ATB_TICK_MS);
    }

    public void stopBattle() {
        battleActive = false;
        if (atbTimer != null) atbTimer.cancel();
    }

    private void updateATB() {
        if (!battleActive) return;
        for (ChronoEntity entity : getAllEntities()) {
            entity.advanceATB();
        }
    }

    private List<ChronoEntity> getAllEntities() {
        List<ChronoEntity> all = new ArrayList<>();
        all.addAll(allies);
        all.addAll(enemies);
        return all;
    }

    public List<String> getMessages() { return messages; }

    public void performAction(ChronoEntity actor, ChronoMove move, ChronoEntity target) {
        if (actor == null || move == null || target == null) return;
        if (("double".equals(move.getMoveType()) || "triple".equals(move.getMoveType())) && move.getRequiredCharacters() != null) {
            for (String name : move.getRequiredCharacters()) {
                ChronoEntity involved = findEntityByName(name, allies);
                if (involved != null) involved.resetATB();
            }
        }
        int actorStrength = 0;
        if (actor.getStatsByLevel() != null && !actor.getStatsByLevel().isEmpty()) {
            actorStrength = actor.getStatsByLevel().get(0).getStrength();
        }
        actorStrength += actor.getTotalBonus("attack");
        if (ChronoEquipmentEffects.isInFrenzyMode(actor)) {
            double frenzyMultiplier = ChronoEquipmentEffects.getFrenzyAttackMultiplier(actor);
            actorStrength = (int) (actorStrength * frenzyMultiplier);
        }
        int targetStamina = 0;
        if (target.getStatsByLevel() != null && !target.getStatsByLevel().isEmpty()) {
            targetStamina = target.getStatsByLevel().get(0).getStamina();
        }
        targetStamina += target.getTotalBonus("defense");
        int damage = Math.max(1, move.getPower() + actorStrength - targetStamina);
        String damageType = move.getType();
        if (damageType != null) {
            double damageModifier = ChronoEquipmentEffects.calculateDamageModifier(target, damageType);
            damage = (int) (damage * damageModifier);
            if (damageModifier == 0.0) {
                String msg = target.getCharacter() + " absorbe completamente el daño " + damageType + "!";
                messages.add(msg);
            } else if (damageModifier < 1.0) {
                String msg = target.getCharacter() + " resiste el daño " + damageType + ".";
                messages.add(msg);
            }
        }
        int oldHp = target.getHp();
        target.setHp(Math.max(0, target.getHp() - damage));
        if (target.getHp() <= 0 && ChronoEquipmentEffects.hasAutoRevive(target)) {
            ChronoEquipmentEffects.consumeAutoRevive(target);
            target.setHp(target.getStatsByLevel().get(0).getHP() / 2);
            String msg = target.getCharacter() + " se revive automáticamente!";
            messages.add(msg);
        }
        if (target.getHp() > 0 && ChronoEquipmentEffects.hasCounterAttack(target)) {
            int counterDamage = Math.max(1, target.getTotalBonus("attack") / 2);
            actor.setHp(Math.max(0, actor.getHp() - counterDamage));
            String msg = target.getCharacter() + " contraataca y causa " + counterDamage + " de daño!";
            messages.add(msg);
        }
        if (move.getCost() > 0) {
            int modifiedCost = ChronoEquipmentEffects.calculateModifiedMPCost(actor, move.getCost());
            actor.setMp(Math.max(0, actor.getMp() - modifiedCost));
        }
        String msg = actor.getCharacter() + " usa " + move.getName() + " contra " + target.getCharacter() + " y causa " + damage + " de daño.";
        if (target.getHp() <= 0 && oldHp > 0) {
            msg += " ¡" + target.getCharacter() + " ha sido derrotado!";
        }
        messages.add(msg);
        actor.resetATB();
        if (allies.contains(actor)) {
            enemyAutoAttack();
        }
    }

    private void enemyAutoAttack() {
        List<ChronoEntity> aliveEnemies = new ArrayList<>();
        for (ChronoEntity enemy : enemies) {
            if (enemy.getHp() > 0) aliveEnemies.add(enemy);
        }
        if (aliveEnemies.isEmpty()) return;
        messages.add("--------------------");
        for (ChronoEntity enemy : aliveEnemies) {
            int turn = enemyTurnCounters.getOrDefault(enemy, 0) + 1;
            enemyTurnCounters.put(enemy, turn);
            ChronoEntity target = getAllyWithLeastHP();
            if (target == null) continue;
            ChronoMove moveToUse = null;
            if (turn % 3 == 0 && enemySpecialAttacks.get(enemy) != null) {
                moveToUse = createSpecialMove(enemySpecialAttacks.get(enemy));
            } else {
                moveToUse = createBasicMove();
            }
            performAction(enemy, moveToUse, target);
        }
        messages.add("--------------------");
    }

    private ChronoEntity getAllyWithLeastHP() {
        ChronoEntity target = null;
        int minHP = 999;
        for (ChronoEntity ally : allies) {
            if (ally.getHp() > 0 && ally.getHp() < minHP) {
                minHP = ally.getHp();
            }
        }
        for (ChronoEntity ally : allies) {
            if (ally.getHp() > 0 && ally.getHp() == minHP) {
                target = ally;
                break;
            }
        }
        return target;
    }

    private ChronoMove createBasicMove() {
        return new ChronoMove("Attack", "physical", 10, 0, "One enemy");
    }
    private ChronoMove createSpecialMove(String name) {
        switch (name.toLowerCase()) {
            case "laser": return new ChronoMove("Laser", "shadow", 50, 0, "One enemy");
            case "darkmatter": return new ChronoMove("DarkMatter", "shadow", 80, 0, "All enemies");
            case "fire": return new ChronoMove("Fire", "fire", 40, 0, "One enemy");
            case "ice": return new ChronoMove("Ice", "ice", 40, 0, "One enemy");
            case "lightning": return new ChronoMove("Lightning", "lightning", 40, 0, "One enemy");
            default: return new ChronoMove(name, "physical", 30, 0, "One enemy");
        }
    }
    private void loadEnemySpecialAttack(ChronoEntity enemy) {
        try {
            String enemyName = enemy.getCharacter().toLowerCase();
            if (enemyName.contains("lavos")) {
                enemySpecialAttacks.put(enemy, "Laser");
            } else if (enemyName.contains("magus")) {
                enemySpecialAttacks.put(enemy, "DarkMatter");
            } else {
                enemySpecialAttacks.put(enemy, "Attack");
            }
        } catch (Exception e) {
            enemySpecialAttacks.put(enemy, "Attack");
        }
    }
    private ChronoEntity findEntityByName(String name, List<ChronoEntity> list) {
        for (ChronoEntity e : list) {
            if (e.getCharacter() != null && e.getCharacter().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }

    // --- Lógica de uso de ítems en batalla ---
    public String useItemOnTarget(String itemName, ChronoEntity user, ChronoEntity target, List<ChronoEntity> allAllies, java.util.function.Function<String, Map<String, Object>> itemLookup) {
        Map<String, Object> itemObj = itemLookup.apply(itemName);
        if (itemObj == null) return "Objeto no encontrado";
        String effect = ((String)itemObj.getOrDefault("effect", "")).toLowerCase();
        String msg = user.getCharacter() + " usa el objeto " + itemName + " en " + target.getCharacter() + ". ";
        if (effect.contains("restores all hp/mp for all")) {
            for (ChronoEntity ally : allAllies) {
                ChronoEntity.StatByLevel stats = ally.getStatsForCurrentLevel();
                int allyMaxHp = stats != null ? stats.getHP() : 9999;
                int allyMaxMp = stats != null ? stats.getMP() : 999;
                ally.setHp(allyMaxHp);
                ally.setMp(allyMaxMp);
            }
            msg += "¡Todo el equipo recupera todo el HP y MP!";
        } else if (effect.contains("restores all hp/mp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxHp = stats != null ? stats.getHP() : 9999;
            int maxMp = stats != null ? stats.getMP() : 999;
            target.setHp(maxHp);
            target.setMp(maxMp);
            msg += "¡Recupera todo el HP y MP!";
        } else if (effect.contains("restores all mp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxMp = stats != null ? stats.getMP() : 999;
            target.setMp(maxMp);
            msg += "¡Recupera todo el MP!";
        } else if (effect.contains("restores all hp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxHp = stats != null ? stats.getHP() : 9999;
            target.setHp(maxHp);
            msg += "¡Recupera todo el HP!";
        } else if (effect.contains("restores 500 hp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxHp = stats != null ? stats.getHP() : 9999;
            target.setHp(Math.min(target.getHp() + 500, maxHp));
            msg += "Recupera 500 HP.";
        } else if (effect.contains("restores 200 hp to all")) {
            for (ChronoEntity ally : allAllies) {
                ChronoEntity.StatByLevel stats = ally.getStatsForCurrentLevel();
                int allyMaxHp = stats != null ? stats.getHP() : 9999;
                int newHp = Math.min(ally.getHp() + 200, allyMaxHp);
                ally.setHp(newHp);
            }
            msg += "¡Todo el equipo recupera 200 HP!";
        } else if (effect.contains("restores 200 hp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxHp = stats != null ? stats.getHP() : 9999;
            target.setHp(Math.min(target.getHp() + 200, maxHp));
            msg += "Recupera 200 HP.";
        } else if (effect.contains("restores 60 mp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxMp = stats != null ? stats.getMP() : 999;
            target.setMp(Math.min(target.getMp() + 60, maxMp));
            msg += "Recupera 60 MP.";
        } else if (effect.contains("restores 30 mp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxMp = stats != null ? stats.getMP() : 999;
            target.setMp(Math.min(target.getMp() + 30, maxMp));
            msg += "Recupera 30 MP.";
        } else if (effect.contains("restores 10 mp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxMp = stats != null ? stats.getMP() : 999;
            target.setMp(Math.min(target.getMp() + 10, maxMp));
            msg += "Recupera 10 MP.";
        } else if (effect.contains("restores 50 hp")) {
            ChronoEntity.StatByLevel stats = target.getStatsForCurrentLevel();
            int maxHp = stats != null ? stats.getHP() : 9999;
            target.setHp(Math.min(target.getHp() + 50, maxHp));
            msg += "Recupera 50 HP.";
        } else if (effect.contains("restores status")) {
            target.setStatus(null);
            msg += "Cura estados alterados.";
        } else {
            msg += "(Efecto no implementado)";
        }
        user.resetATB();
        messages.add(msg);
        return msg;
    }
} 