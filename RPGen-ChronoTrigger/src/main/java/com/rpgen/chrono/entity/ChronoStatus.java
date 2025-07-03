package com.rpgen.chrono.entity;

public class ChronoStatus {
    private String name;
    private String description;
    private int duration; // en turnos o ticks
    private String effect; // descripción del efecto

    public ChronoStatus(String name, String description, int duration, String effect) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public String getEffect() {
        return effect;
    }
} 