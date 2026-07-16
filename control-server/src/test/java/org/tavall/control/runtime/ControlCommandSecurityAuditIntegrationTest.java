package org.tavall.control.runtime;

import org.tavall.control.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ControlCommandSecurityAuditIntegrationTest {
    @Test
    void unauthorizedOperatorIsRejectedAndAudited() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator viewer = new ControlOperator(UUID.randomUUID(), Optional.empty(), "viewer", ControlOperatorRole.VIEWER, true, Instant.now(), Map.of());
        ControlCommand command = command(ControlCommandType.GIVE_RESOURCE, viewer, Map.of(
                "universalPlayerId", UniversalPlayerId.random().value().toString(),
                "globalAssetId", "item.healing.field_rations",
                "amount", "10"
        ), false);

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);
        ControlCommandAuditLog auditLog = runtime.auditLogRepository().findAuditLogsForCommand(command.commandId()).getFirst();

        assertEquals(CommandExecutionState.REJECTED, result.state());
        assertFalse(result.success());
        assertTrue(result.message().contains("Operator lacks permission"));
        assertEquals(CommandExecutionState.REJECTED, auditLog.resultState());
        assertFalse(auditLog.success());
    }

    @Test
    void highRiskCommandRequiresElevatedRoleAndRedactsSensitiveAuditArguments() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator operator = new ControlOperator(UUID.randomUUID(), Optional.empty(), "operator", ControlOperatorRole.OPERATOR, true, Instant.now(), Map.of());
        ControlCommand command = new ControlCommand(
                ControlCommandId.random(),
                ControlCommandType.GIVE_RESOURCE,
                operator,
                CommandIssuedFrom.WEB_PANEL,
                CommandTargetScope.PLAYER,
                Set.of(),
                Map.of(
                        "universalPlayerId", UniversalPlayerId.random().value().toString(),
                        "globalAssetId", "item.healing.field_rations",
                        "amount", "10",
                        "sessionToken", "never-log-this"
                ),
                false,
                Instant.now(),
                Map.of()
        );

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);
        ControlCommandAuditLog auditLog = runtime.auditLogRepository().findAuditLogsForCommand(command.commandId()).getFirst();

        assertEquals(CommandExecutionState.REJECTED, result.state());
        assertTrue(result.message().contains("High-risk command requires"));
        assertEquals("[REDACTED]", auditLog.argumentsRedacted().get("sessionToken"));
        assertEquals("10", auditLog.argumentsRedacted().get("amount"));
    }

    @Test
    void dryRunIsAuditedAndDoesNotCreatePlatformFanout() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        ControlOperator owner = ControlOperator.localOwner(Instant.now());
        ControlCommand command = new ControlCommand(
                ControlCommandId.random(),
                ControlCommandType.BROADCAST_PLATFORM_MESSAGE,
                owner,
                CommandIssuedFrom.CLI,
                CommandTargetScope.GLOBAL,
                Set.of(),
                Map.of("message", "dry run only"),
                true,
                Instant.now(),
                Map.of()
        );

        ControlCommandResult result = runtime.dispatchHandler().dispatchCommand(command);
        ControlCommandAuditLog auditLog = runtime.auditLogRepository().findAuditLogsForCommand(command.commandId()).getFirst();

        assertEquals(CommandExecutionState.DRY_RUN_COMPLETED, result.state());
        assertTrue(result.platformResults().isEmpty());
        assertTrue(auditLog.dryRun());
    }

    private ControlCommand command(ControlCommandType commandType, ControlOperator operator, Map<String, String> arguments, boolean dryRun) {
        return new ControlCommand(ControlCommandId.random(), commandType, operator, CommandIssuedFrom.CLI, CommandTargetScope.PLAYER, Set.of(), arguments, dryRun, Instant.now(), Map.of());
    }
}
