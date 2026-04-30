package com.tavall.hytale.resourcegame.middleware.troop;

public final class WoundedTroopCaptureHandler {
    private final TroopStatusTransitionHandler troopStatusTransitionHandler;

    public WoundedTroopCaptureHandler(TroopStatusTransitionHandler troopStatusTransitionHandler) {
        this.troopStatusTransitionHandler = troopStatusTransitionHandler;
    }

    public Troop captureWoundedTroop(TroopId troopId) {
        return troopStatusTransitionHandler.transitionStatus(troopId, TroopStatus.CAPTURED);
    }
}
