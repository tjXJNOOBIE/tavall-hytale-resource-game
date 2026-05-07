package com.tavall.hytale.resourcegame.middleware.citizen;

public final class CitizenMoraleEffectHandler {
    public double modifier(CitizenMoraleState moraleState) {
        return switch (moraleState) {
            case HIGH -> 1.15;
            case MEDIUM -> 1.0;
            case LOW -> 0.75;
            case POOR -> 0.45;
        };
    }
}
