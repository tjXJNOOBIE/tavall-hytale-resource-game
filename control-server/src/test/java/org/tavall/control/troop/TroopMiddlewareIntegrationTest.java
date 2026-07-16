package org.tavall.control.troop;

import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class TroopMiddlewareIntegrationTest {
    @Test
    void troopTierRangeIsValidatedAndStatusTransitionsAreSafe() {
        InMemoryTroopRepository repository = new InMemoryTroopRepository();
        TroopRegistrationHandler registrationHandler = new TroopRegistrationHandler(repository);
        TroopStatusTransitionHandler transitionHandler = new TroopStatusTransitionHandler(repository);

        assertThrows(TroopValidationException.class, () -> registrationHandler.registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 11, new CanonicalLocation("world", 0.0d, 0.0d, 0.0d)));

        Troop troop = registrationHandler.registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 1, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
        Troop wounded = transitionHandler.transitionStatus(troop.troopId(), TroopStatus.WOUNDED);
        Troop captured = new WoundedTroopCaptureHandler(transitionHandler).captureWoundedTroop(wounded.troopId());

        assertEquals(TroopStatus.CAPTURED, captured.status());
        assertEquals("troop.infantry.tier_1", captured.globalAssetId().value());
        assertThrows(TroopValidationException.class, () -> transitionHandler.transitionStatus(captured.troopId(), TroopStatus.FIGHTING));
    }
}
