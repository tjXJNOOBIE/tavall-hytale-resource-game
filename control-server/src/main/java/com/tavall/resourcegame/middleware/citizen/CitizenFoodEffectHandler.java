package com.tavall.resourcegame.middleware.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

public final class CitizenFoodEffectHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public CitizenConditionEffectResult evaluate(CitizenData citizen) {
        return switch (citizen.nutritionState()) {
            case FED -> new CitizenConditionEffectResult(citizen, 1.0, 1.0, "food-sufficient");
            case LOW_FOOD -> new CitizenConditionEffectResult(citizen, 0.75, 0.65, "food-low");
            case STARVING -> new CitizenConditionEffectResult(citizen, 0.25, 0.0, "food-critical");
            case UNKNOWN -> new CitizenConditionEffectResult(citizen, 0.9, 0.85, "food-unknown");
        };
    }
}
