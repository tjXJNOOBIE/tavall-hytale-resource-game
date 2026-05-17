package com.tavall.resourcegame.middleware.control;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.middleware.clock.AgingTickResult;
import com.tavall.resourcegame.middleware.clock.KingdomClockTickResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class ControlPlaneMaintenanceWorker implements IControlCommandDomain, IDependencyInjectableConcrete {
    private static final int DEFAULT_LIMIT = 50;

    public ControlPlaneMaintenanceResult runOnce() {
        return runOnce(DEFAULT_LIMIT);
    }

    public ControlPlaneMaintenanceResult runOnce(int limit) {
        Instant now = getControlCommandClock().instant();
        int effectiveLimit = Math.max(0, limit);
        List<ControlCommandResult> scheduledResults = getControlCommandSchedulingHandler().dispatchDueCommands(now, effectiveLimit);
        List<KingdomClockTickResult> tickResults = getKingdomClockControlSystem().tickAllKingdomClocks();
        int agingTicks = 0;
        int scheduledStateChanges = 0;
        for (KingdomClockTickResult tickResult : tickResults) {
            AgingTickResult agingTickResult = getKingdomClockControlSystem().runAgingTick(tickResult.kingdomId());
            if (!agingTickResult.eventsEmitted().isEmpty()) {
                agingTicks++;
            }
            scheduledStateChanges += getKingdomClockControlSystem().applyScheduledStateChanges(tickResult.kingdomId()).appliedEffects().size();
        }
        getUniversalKingdomSimulationSystem().evaluateScaling(now);
        List<ControlPlatformFanoutRetryRecord> dueRetries = getControlPlatformFanoutRetryHandler().dueRetries(now, effectiveLimit);
        dueRetries.forEach(retry -> getControlPlatformFanoutRetryHandler().markRetryAttempted(
                retry.retryId(),
                false,
                now,
                "Retry replay requires live platform adapter payload; marked for scheduler review."
        ));
        return new ControlPlaneMaintenanceResult(
                now,
                scheduledResults.size(),
                tickResults.size(),
                agingTicks,
                1,
                scheduledStateChanges,
                dueRetries.size(),
                scheduledResults,
                Map.of("canonicalOwner", "plain-java-control-server")
        );
    }
}
