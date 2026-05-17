package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

public final class CitizenMoraleEffectHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public double modifier(CitizenMoraleState moraleState) {
        return switch (moraleState) {
            case HIGH -> 1.15;
            case MEDIUM -> 1.0;
            case LOW -> 0.75;
            case POOR -> 0.45;
        };
    }
}
