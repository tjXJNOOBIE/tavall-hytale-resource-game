package org.tavall.control.troop;

public final class WoundedTroopCaptureHandler implements ITroopDomain {
    public WoundedTroopCaptureHandler() {
    }

    public WoundedTroopCaptureHandler(TroopStatusTransitionHandler troopStatusTransitionHandler) {
        registerTroopStatusTransitionHandler(troopStatusTransitionHandler);
    }

    public Troop captureWoundedTroop(TroopId troopId) {
        return getTroopStatusTransitionHandler().transitionStatus(troopId, TroopStatus.CAPTURED);
    }
}
