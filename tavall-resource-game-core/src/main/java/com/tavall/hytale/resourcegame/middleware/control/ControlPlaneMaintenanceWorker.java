package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.clock.AgingTickResult;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockControlSystem;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockTickResult;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ControlPlaneMaintenanceWorker {
    private static final int DEFAULT_LIMIT = 50;

    private final ControlCommandSchedulingHandler schedulingHandler;
    private final ControlPlatformFanoutRetryHandler fanoutRetryHandler;
    private final KingdomClockControlSystem kingdomClockSystem;
    private final UniversalKingdomSimulationSystem kingdomSimulationSystem;
    private final Clock clock;

    public ControlPlaneMaintenanceWorker(
            ControlCommandSchedulingHandler schedulingHandler,
            ControlPlatformFanoutRetryHandler fanoutRetryHandler,
            KingdomClockControlSystem kingdomClockSystem,
            UniversalKingdomSimulationSystem kingdomSimulationSystem,
            Clock clock
    ) {
        this.schedulingHandler = Objects.requireNonNull(schedulingHandler, "schedulingHandler");
        this.fanoutRetryHandler = Objects.requireNonNull(fanoutRetryHandler, "fanoutRetryHandler");
        this.kingdomClockSystem = Objects.requireNonNull(kingdomClockSystem, "kingdomClockSystem");
        this.kingdomSimulationSystem = Objects.requireNonNull(kingdomSimulationSystem, "kingdomSimulationSystem");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public ControlPlaneMaintenanceResult runOnce() {
        return runOnce(DEFAULT_LIMIT);
    }

    public ControlPlaneMaintenanceResult runOnce(int limit) {
        Instant now = clock.instant();
        int effectiveLimit = Math.max(0, limit);
        List<ControlCommandResult> scheduledResults = schedulingHandler.dispatchDueCommands(now, effectiveLimit);
        List<KingdomClockTickResult> tickResults = kingdomClockSystem.tickAllKingdomClocks();
        int agingTicks = 0;
        int scheduledStateChanges = 0;
        for (KingdomClockTickResult tickResult : tickResults) {
            AgingTickResult agingTickResult = kingdomClockSystem.runAgingTick(tickResult.kingdomId());
            if (!agingTickResult.eventsEmitted().isEmpty()) {
                agingTicks++;
            }
            scheduledStateChanges += kingdomClockSystem.applyScheduledStateChanges(tickResult.kingdomId()).appliedEffects().size();
        }
        kingdomSimulationSystem.evaluateScaling(now);
        List<ControlPlatformFanoutRetryRecord> dueRetries = fanoutRetryHandler.dueRetries(now, effectiveLimit);
        dueRetries.forEach(retry -> fanoutRetryHandler.markRetryAttempted(
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
