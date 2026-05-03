package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.healing.GemType;
import com.tavall.hytale.resourcegame.middleware.healing.HealingItemType;
import com.tavall.hytale.resourcegame.middleware.healing.HealingMode;
import com.tavall.hytale.resourcegame.middleware.healing.HealingState;
import com.tavall.hytale.resourcegame.middleware.healing.WoundSeverity;
import com.tavall.hytale.resourcegame.middleware.healing.WoundType;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;
import com.tavall.hytale.resourcegame.middleware.troop.TroopId;
import com.tavall.hytale.resourcegame.middleware.troop.TroopRegistrationHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlCommandDispatchIntegrationTest {
    @Test
    void dryRunAssignWoundDoesNotMutateCanonicalTroopStateOrFanout() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator operator = ControlOperator.localOwner(Instant.now());
        Troop troop = registerTroop(runtime, UniversalPlayerId.random());
        ControlCommand command = command(
                ControlCommandType.ASSIGN_TROOP_WOUND,
                operator,
                CommandTargetScope.TROOP,
                Map.of("troopId", troop.troopId().value().toString(), "woundType", WoundType.GENERAL_WOUND.name(), "severity", WoundSeverity.MINOR.name()),
                true
        );

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);

        assertEquals(CommandExecutionState.DRY_RUN_COMPLETED, result.state());
        assertTrue(result.success());
        assertTrue(runtime.troopHealingRepository().findActiveWoundsForTroop(troop.troopId()).isEmpty());
        assertTrue(result.platformResults().isEmpty());
        assertEquals(1, runtime.auditLogRepository().findAuditLogsForCommand(command.commandId()).size());
    }

    @Test
    void executeAssignWoundMutatesCanonicalStateAndFansOutToAllPlatforms() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator operator = ControlOperator.localOwner(Instant.now());
        Troop troop = registerTroop(runtime, UniversalPlayerId.random());
        ControlCommand command = command(
                ControlCommandType.ASSIGN_TROOP_WOUND,
                operator,
                CommandTargetScope.TROOP,
                Map.of("troopId", troop.troopId().value().toString(), "woundType", WoundType.GENERAL_WOUND.name(), "severity", WoundSeverity.MODERATE.name()),
                false
        );

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);

        assertEquals(CommandExecutionState.COMPLETED, result.state());
        assertEquals(4, result.platformResults().size());
        assertTrue(result.platformResults().stream().allMatch(PlatformCommandResult::success));
        assertEquals(1, runtime.troopHealingRepository().findActiveWoundsForTroop(troop.troopId()).size());
        assertTrue(result.changedObjectIds().stream().anyMatch(id -> id.startsWith("wound:")));
        assertTrue(((InMemoryPlatformFrontendAdapter) runtime.fanoutHandler().adaptersByPlatform().get(GamePlatform.ROBLOX)).refreshedProjectionIds().stream()
                .anyMatch(id -> id.contains("roblox")));
        assertTrue(((InMemoryPlatformFrontendAdapter) runtime.fanoutHandler().adaptersByPlatform().get(GamePlatform.DISCORD)).refreshedProjectionIds().stream()
                .anyMatch(id -> id.contains("discord")));
    }

    @Test
    void healingCommandsStartAndCompletePlanThroughSharedCommandPipeline() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator operator = ControlOperator.localOwner(Instant.now());
        UniversalPlayerId playerId = UniversalPlayerId.random();
        Troop troop = registerTroop(runtime, playerId);

        runtime.dispatchHandler().dispatchCommand(command(
                ControlCommandType.ASSIGN_TROOP_WOUND,
                operator,
                CommandTargetScope.TROOP,
                Map.of("troopId", troop.troopId().value().toString(), "woundType", WoundType.GENERAL_WOUND.name(), "severity", WoundSeverity.MINOR.name()),
                false
        ));
        grant(runtime, operator, playerId, HealingItemType.FIELD_RATIONS.globalAssetId().value(), 20);
        grant(runtime, operator, playerId, HealingItemType.BANDAGE_KIT.globalAssetId().value(), 20);
        grant(runtime, operator, playerId, GemType.PEARL.globalAssetId().value(), 5);

        ControlCommandResult startResult = runtime.dispatchHandler().dispatchCommand(command(
                ControlCommandType.START_TROOP_HEALING,
                operator,
                CommandTargetScope.TROOP,
                Map.of(
                        "universalPlayerId", playerId.value().toString(),
                        "troopId", troop.troopId().value().toString(),
                        "healingMode", HealingMode.PROPER_TREATMENT.name(),
                        "recipeId", "recipe.healing.general_wound.proper",
                        "facilityLevel", "4"
                ),
                false
        ));
        String healingPlanId = startResult.changedObjectIds().stream()
                .filter(id -> id.startsWith("healingPlan:"))
                .map(id -> id.substring("healingPlan:".length()))
                .findFirst()
                .orElseThrow();
        ControlCommandResult tickResult = runtime.dispatchHandler().dispatchCommand(command(
                ControlCommandType.RUN_HEALING_TICK,
                operator,
                CommandTargetScope.GLOBAL,
                Map.of("tickCount", "1"),
                false
        ));

        assertEquals(CommandExecutionState.COMPLETED, startResult.state());
        assertEquals(HealingState.COMPLETED, runtime.troopHealingRepository().findHealingPlan(java.util.UUID.fromString(healingPlanId)).orElseThrow().state());
        assertEquals(CommandExecutionState.COMPLETED, tickResult.state());
        assertTrue(tickResult.platformResults().stream().anyMatch(result -> result.platform() == GamePlatform.MINECRAFT && result.success()));
    }

    @Test
    void platformOfflineCreatesPartialResultWithoutRollingBackCanonicalState() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ((InMemoryPlatformFrontendAdapter) runtime.fanoutHandler().adaptersByPlatform().get(GamePlatform.HYTALE)).setConnected(false);
        ControlOperator operator = ControlOperator.localOwner(Instant.now());
        Troop troop = registerTroop(runtime, UniversalPlayerId.random());

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command(
                ControlCommandType.ASSIGN_TROOP_WOUND,
                operator,
                CommandTargetScope.TROOP,
                Map.of("troopId", troop.troopId().value().toString(), "woundType", WoundType.POISONED.name(), "severity", WoundSeverity.MODERATE.name()),
                false
        ));

        assertEquals(CommandExecutionState.PARTIALLY_COMPLETED, result.state());
        assertFalse(result.success());
        assertTrue(result.platformResults().stream().anyMatch(platformResult -> platformResult.platform() == GamePlatform.HYTALE && !platformResult.success()));
        assertEquals(1, runtime.troopHealingRepository().findActiveWoundsForTroop(troop.troopId()).size());
        assertTrue(runtime.resultRepository().findResult(result.commandId()).isPresent());
    }

    @Test
    void controlStartWebPanelUsesLaunchPortWithoutEmbeddingSpringInCanonicalRuntime() {
        RecordingControlSurfaceLaunchHandler launchHandler = new RecordingControlSurfaceLaunchHandler();
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime(launchHandler);
        ControlOperator operator = ControlOperator.localOwner(Instant.now());

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command(
                ControlCommandType.START_CONTROL_SURFACE,
                operator,
                CommandTargetScope.GLOBAL,
                Map.of("surface", "web-panel", "port", "18093"),
                false
        ));

        assertEquals(CommandExecutionState.COMPLETED, result.state());
        assertTrue(result.success());
        assertEquals(1, launchHandler.launchResults().size());
        assertEquals("18093", launchHandler.launchResults().getFirst().metadata().get("port"));
        assertTrue(result.changedObjectIds().contains("control-surface:web-panel"));
    }

    private Troop registerTroop(ControlCommandRuntime runtime, UniversalPlayerId playerId) {
        return new TroopRegistrationHandler(runtime.troopRepository())
                .registerTroop(Optional.of(playerId), Optional.empty(), "infantry", 2, new CanonicalLocation("world", 0.0d, 64.0d, 0.0d));
    }

    private void grant(ControlCommandRuntime runtime, ControlOperator operator, UniversalPlayerId playerId, String globalAssetId, int amount) {
        runtime.dispatchHandler().dispatchCommand(command(
                ControlCommandType.GIVE_RESOURCE,
                operator,
                CommandTargetScope.PLAYER,
                Map.of("universalPlayerId", playerId.value().toString(), "globalAssetId", globalAssetId, "amount", Integer.toString(amount)),
                false
        ));
    }

    private ControlCommand command(ControlCommandType commandType, ControlOperator operator, CommandTargetScope targetScope, Map<String, String> arguments, boolean dryRun) {
        return new ControlCommand(ControlCommandId.random(), commandType, operator, CommandIssuedFrom.CLI, targetScope, Set.of(), arguments, dryRun, Instant.now(), Map.of());
    }
}
