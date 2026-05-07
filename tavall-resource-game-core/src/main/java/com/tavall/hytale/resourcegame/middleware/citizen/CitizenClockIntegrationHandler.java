package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockControlSystem;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockState;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomTimePhase;

public final class CitizenClockIntegrationHandler {
    private final KingdomClockControlSystem clockControlSystem;

    public CitizenClockIntegrationHandler(KingdomClockControlSystem clockControlSystem) {
        this.clockControlSystem = clockControlSystem;
    }

    public KingdomClockState clockState(String kingdomId) {
        return clockControlSystem.getCurrentClockState(kingdomId);
    }

    public double productivityModifier(String kingdomId) {
        KingdomTimePhase phase = clockState(kingdomId).currentPhase();
        return switch (phase) {
            case DAWN -> 0.85;
            case DAY -> 1.0;
            case DUSK -> 0.75;
            case NIGHT -> 0.35;
        };
    }
}
