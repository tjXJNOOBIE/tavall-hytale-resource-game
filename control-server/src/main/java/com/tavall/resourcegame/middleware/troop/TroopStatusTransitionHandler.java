package org.tavall.control.troop;

import java.util.Map;
import java.util.Set;

public final class TroopStatusTransitionHandler implements ITroopDomain {
    private static final Map<TroopStatus, Set<TroopStatus>> ALLOWED_TRANSITIONS = Map.of(
            TroopStatus.IDLE, Set.of(TroopStatus.TRAINING, TroopStatus.MARCHING, TroopStatus.DEFENDING, TroopStatus.WOUNDED),
            TroopStatus.TRAINING, Set.of(TroopStatus.IDLE),
            TroopStatus.MARCHING, Set.of(TroopStatus.FIGHTING, TroopStatus.DEFENDING, TroopStatus.IDLE, TroopStatus.WOUNDED),
            TroopStatus.DEFENDING, Set.of(TroopStatus.FIGHTING, TroopStatus.IDLE, TroopStatus.WOUNDED),
            TroopStatus.FIGHTING, Set.of(TroopStatus.WOUNDED, TroopStatus.DEAD, TroopStatus.IDLE),
            TroopStatus.WOUNDED, Set.of(TroopStatus.CAPTURED, TroopStatus.DEAD, TroopStatus.IDLE),
            TroopStatus.CAPTURED, Set.of(TroopStatus.IDLE, TroopStatus.DEAD),
            TroopStatus.DEAD, Set.of()
    );

    public TroopStatusTransitionHandler() {
    }

    public TroopStatusTransitionHandler(TroopRepository troopRepository) {
        registerTroopRepository(troopRepository);
    }

    public Troop transitionStatus(TroopId troopId, TroopStatus targetStatus) {
        Troop troop = getTroopRepository().findTroop(troopId)
                .orElseThrow(() -> new TroopValidationException("Troop was not found."));
        if (!ALLOWED_TRANSITIONS.getOrDefault(troop.status(), Set.of()).contains(targetStatus)) {
            throw new TroopValidationException("Invalid troop status transition " + troop.status() + " -> " + targetStatus + ".");
        }
        return getTroopRepository().saveTroop(troop.withStatus(targetStatus));
    }
}
