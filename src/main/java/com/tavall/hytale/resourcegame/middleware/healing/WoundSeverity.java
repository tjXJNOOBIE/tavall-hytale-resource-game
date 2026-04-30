package com.tavall.hytale.resourcegame.middleware.healing;

public enum WoundSeverity {
    MINOR(1.0d),
    MODERATE(1.5d),
    SEVERE(2.25d),
    CRITICAL(3.5d);

    private final double costMultiplier;

    WoundSeverity(double costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    public double costMultiplier() {
        return costMultiplier;
    }
}
