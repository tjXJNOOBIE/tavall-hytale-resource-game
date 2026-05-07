package com.tavall.hytale.resourcegame.middleware.citizen;

import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockState;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomTimePhase;

public final class CitizenNightRestEffectHandler {
    public double modifier(KingdomClockState clockState) {
        if (clockState == null) {
            return 1.0;
        }
        return clockState.currentPhase() == KingdomTimePhase.NIGHT ? 1.05 : 1.0;
    }
}
