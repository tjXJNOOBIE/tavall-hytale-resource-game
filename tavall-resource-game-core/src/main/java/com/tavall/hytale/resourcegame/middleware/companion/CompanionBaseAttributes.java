package com.tavall.hytale.resourcegame.middleware.companion;

public record CompanionBaseAttributes(
        double intel,
        double strength,
        double agility
) {
    public CompanionBaseAttributes {
        if (intel < 0 || strength < 0 || agility < 0) {
            throw new IllegalArgumentException("Companion base attributes cannot be negative.");
        }
    }
}
