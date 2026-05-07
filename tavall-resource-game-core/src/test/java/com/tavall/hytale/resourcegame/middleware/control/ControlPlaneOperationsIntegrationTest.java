package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.healing.WoundSeverity;
import com.tavall.hytale.resourcegame.middleware.healing.WoundType;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlPlaneOperationsIntegrationTest {
    @Test
    void failedPlatformFanoutCreatesDurableRetryRecordAndCompensationDecision() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ((InMemoryPlatformFrontendAdapter) runtime.fanoutHandler().adaptersByPlatform().get(GamePlatform.HYTALE)).setConnected(false);
        Troop troop = registerTroop(runtime);
        ControlCommand command = woundCommand(troop, ControlOperator.localOwner(Instant.now()), false);

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);
        ControlCommandCompensationDecision compensationDecision = runtime.compensationHandler().evaluateResult(command, result);

        assertEquals(CommandExecutionState.PARTIALLY_COMPLETED, result.state());
        assertFalse(result.success());
        assertEquals(1, runtime.fanoutRetryRepository().findRetriesForCommand(command.commandId()).size());
        ControlPlatformFanoutRetryRecord retryRecord = runtime.fanoutRetryRepository().findRetriesForCommand(command.commandId()).getFirst();
        assertEquals(GamePlatform.HYTALE, retryRecord.platform());
        assertEquals(ControlPlatformFanoutRetryState.PENDING, retryRecord.state());
        assertTrue(compensationDecision.manualReviewRequired());
        assertFalse(compensationDecision.compensationRequired());
    }

    @Test
    void scheduledCommandDispatchesThroughSameControlPipeline() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        Troop troop = registerTroop(runtime);
        Instant now = Instant.parse("2026-04-30T18:30:00Z");
        ControlCommand command = woundCommand(troop, ControlOperator.localOwner(now), false);
        ScheduledControlCommand scheduledCommand = runtime.schedulingHandler().scheduleCommand(command, now.minusSeconds(1), now);

        ControlCommandResult result = runtime.schedulingHandler().dispatchDueCommands(now, 10).getFirst();
        ScheduledControlCommand dispatched = runtime.scheduledCommandRepository().findScheduledCommand(scheduledCommand.scheduleId()).orElseThrow();

        assertEquals(CommandExecutionState.COMPLETED, result.state());
        assertEquals(ScheduledControlCommandState.DISPATCHED, dispatched.state());
        assertTrue(dispatched.dispatchedAt().isPresent());
        assertEquals(1, runtime.troopHealingRepository().findActiveWoundsForTroop(troop.troopId()).size());
    }

    @Test
    void maintenanceWorkerDispatchesDueCommandsTicksClocksAndMarksDueRetriesForReview() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ((InMemoryPlatformFrontendAdapter) runtime.fanoutHandler().adaptersByPlatform().get(GamePlatform.HYTALE)).setConnected(false);
        Troop troop = registerTroop(runtime);
        Instant now = Instant.parse("2026-04-30T18:30:00Z");
        ControlCommand scheduled = woundCommand(troop, ControlOperator.localOwner(now), false);
        runtime.schedulingHandler().scheduleCommand(scheduled, now.minusSeconds(1), now);
        ControlCommand failedFanout = woundCommand(troop, ControlOperator.localOwner(now), false);
        runtime.fanoutRetryRepository().saveRetryRecord(new ControlPlatformFanoutRetryRecord(
                java.util.UUID.randomUUID(),
                failedFanout.commandId(),
                failedFanout.commandType(),
                GamePlatform.HYTALE,
                List.of("troop:" + troop.troopId()),
                0,
                5,
                now.minusSeconds(1),
                Optional.empty(),
                ControlPlatformFanoutRetryState.PENDING,
                "offline",
                now.minusSeconds(60),
                now.minusSeconds(60),
                Map.of()
        ));

        ControlPlaneMaintenanceResult result = runtime.maintenanceWorker().runOnce(10);

        assertEquals(1, result.scheduledCommandsDispatched());
        assertTrue(result.kingdomClocksTicked() >= 1);
        assertEquals(1, result.kingdomScalingEvaluations());
        assertEquals(1, result.fanoutRetriesMarkedForReview());
        ControlPlatformFanoutRetryRecord retryRecord = runtime.fanoutRetryRepository().findRetriesForCommand(failedFanout.commandId()).getFirst();
        assertEquals(ControlPlatformFanoutRetryState.RETRYING, retryRecord.state());
        assertTrue(retryRecord.message().contains("scheduler review"));
    }

    private Troop registerTroop(ControlCommandRuntime runtime) {
        return new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(UniversalPlayerId.random()), Optional.empty(), "infantry", 1, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
    }

    private ControlCommand woundCommand(Troop troop, ControlOperator operator, boolean dryRun) {
        return new ControlCommand(
                ControlCommandId.random(),
                ControlCommandType.ASSIGN_TROOP_WOUND,
                operator,
                CommandIssuedFrom.SYSTEM,
                CommandTargetScope.TROOP,
                Set.of(),
                Map.of("troopId", troop.troopId().value().toString(), "woundType", WoundType.GENERAL_WOUND.name(), "severity", WoundSeverity.MINOR.name()),
                dryRun,
                Instant.now(),
                Map.of()
        );
    }
}
