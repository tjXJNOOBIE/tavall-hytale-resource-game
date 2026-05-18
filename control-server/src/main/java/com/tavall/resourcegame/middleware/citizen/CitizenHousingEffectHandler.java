package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class CitizenHousingEffectHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public double modifier(CitizenHousingState housingState) {
        return switch (housingState) {
            case HOUSED -> 1.0;
            case CROWDED -> 0.85;
            case HOMELESS -> 0.6;
            case UNKNOWN -> 0.9;
        };
    }
}
