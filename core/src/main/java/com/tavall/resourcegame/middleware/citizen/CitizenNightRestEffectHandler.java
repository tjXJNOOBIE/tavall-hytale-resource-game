package com.tavall.resourcegame.middleware.citizen;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.clock.KingdomClockState;
import com.tavall.resourcegame.middleware.clock.KingdomTimePhase;

public final class CitizenNightRestEffectHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public double modifier(KingdomClockState clockState) {
        if (clockState == null) {
            return 1.0;
        }
        return clockState.currentPhase() == KingdomTimePhase.NIGHT ? 1.05 : 1.0;
    }
}
