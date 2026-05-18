package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.clock.KingdomClockState;
import org.tavall.control.clock.KingdomTimePhase;

public final class CitizenNightRestEffectHandler implements ICitizenDomain, IDependencyInjectableConcrete {
    public double modifier(KingdomClockState clockState) {
        if (clockState == null) {
            return 1.0;
        }
        return clockState.currentPhase() == KingdomTimePhase.NIGHT ? 1.05 : 1.0;
    }
}
