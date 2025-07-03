package com.rpgen.chrono.entity;

import com.rpgen.core.action.GameAction;
import com.rpgen.core.entity.Entity;
import java.util.*;

public class ChronoMove implements GameAction {
    private String name;
    private String type;
    private int power;
    private int cost;
    private String target;
    private String moveType; // single, double, triple
    private List<String> requiredCharacters;
    private String description;
    private String owner;

    public ChronoMove(String name, String type, int power, int cost, String target) {
        this.name = name;
        this.type = type;
        this.power = power;
        this.cost = cost;
        this.target = target;
    }

    @Override
    public String getId() { return name != null ? name : ""; }
    @Override
    public String getName() { return name != null ? name : ""; }
    @Override
    public String getDescription() { return description != null ? description : ""; }
    @Override
    public int getCooldown() { return 0; }
    @Override
    public void execute(Entity source, Entity target) {
        // Lógica básica: inflige daño igual a power
        if (source != null && target != null && source.isAlive() && target.isAlive()) {
            target.takeDamage(power);
        }
    }
    @Override
    public boolean canExecute(Entity source, Entity target) {
        return source != null && target != null && source.isAlive() && target.isAlive();
    }
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("type", type);
        props.put("power", power);
        props.put("cost", cost);
        props.put("target", target);
        props.put("moveType", moveType);
        props.put("requiredCharacters", requiredCharacters);
        props.put("owner", owner);
        return props;
    }

    public String getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getCost() {
        return cost;
    }

    public String getTarget() {
        return target;
    }

    public void setType(String moveType) { this.moveType = moveType; }
    public String getMoveType() { return moveType; }
    public void setRequiredCharacters(List<String> chars) { this.requiredCharacters = chars; }
    public List<String> getRequiredCharacters() { return requiredCharacters; }
    public void setDescription(String desc) { this.description = desc; }
    public String getDescriptionField() { return description; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getOwner() { return owner; }
} 