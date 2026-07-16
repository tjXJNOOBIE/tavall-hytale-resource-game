package org.tavall.control.citizen;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.clock.KingdomClockControlSystem;
import org.tavall.control.clock.KingdomClockState;
import org.tavall.control.clock.KingdomTimePhase;

public final class CitizenClockIntegrationHandler implements CitizenDomain, IDependencyInjectableConcrete {
    public CitizenClockIntegrationHandler() {
    }

    public CitizenClockIntegrationHandler(KingdomClockControlSystem clockControlSystem) {
        registerCitizenDependency(KingdomClockControlSystem.class, clockControlSystem);
    }

    public KingdomClockState clockState(String kingdomId) {
        return getKingdomClockControlSystem().getCurrentClockState(kingdomId);
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
