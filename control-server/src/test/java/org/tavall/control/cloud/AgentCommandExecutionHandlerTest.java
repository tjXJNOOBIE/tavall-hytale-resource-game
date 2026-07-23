package org.tavall.control.cloud;

import org.tavall.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AgentCommandExecutionHandlerTest implements CloudAgentDomain {
    @Test
    void agentAcceptsTypedAllowlistedCommands() {
        DependencyLoaderAccess.clear();
        new CloudAgentDependencyModule().registerDependencies();
        Instant now = Instant.parse("2026-05-08T18:00:00Z");

        CloudCommandResult result = getAgentCommandExecutionHandler().execute(command(CloudCommandType.RUN_HEALTH_CHECK, now, Optional.empty()), now);

        assertTrue(result.success());
        assertTrue(result.message().contains("Accepted typed command RUN_HEALTH_CHECK."));
    }

    @Test
    void agentRejectsExpiredCommand() {
        DependencyLoaderAccess.clear();
        new CloudAgentDependencyModule().registerDependencies();
        Instant now = Instant.parse("2026-05-08T18:00:00Z");

        CloudCommandResult result = getAgentCommandExecutionHandler().execute(command(CloudCommandType.START_WORKLOAD,
                now.minusSeconds(60), Optional.of(now.minusSeconds(1)), "{\"runtime\":\"tmux\"}"), now);

        assertFalse(result.success());
        assertTrue(result.errorSummary().contains("expired"));
    }

    @Test
    void agentRejectsUntypedCommand() {
        DependencyLoaderAccess.clear();
        new CloudAgentDependencyModule().registerDependencies();
        Instant now = Instant.parse("2026-05-08T18:00:00Z");

        CloudCommandResult result = getAgentCommandExecutionHandler().execute(command(null, now, Optional.empty()), now);

        assertFalse(result.success());
        assertTrue(result.errorSummary().contains("command type"));
    }

    @Test
    void agentRoutesWorkloadCommandsToDeclaredRuntimeAdapters() {
        DependencyLoaderAccess.clear();
        new CloudAgentDependencyModule().registerDependencies();
        Instant now = Instant.parse("2026-05-08T18:00:00Z");

        CloudCommandResult tmuxResult = getAgentCommandExecutionHandler()
                .execute(command(CloudCommandType.START_WORKLOAD, now, Optional.empty(), "{\"runtime\":\"tmux\",\"workloadId\":\"minecraft-kingdom-1\",\"sessionName\":\"minecraft-kingdom\"}"), now);
        CloudCommandResult missingRuntimeResult = getAgentCommandExecutionHandler()
                .execute(command(CloudCommandType.START_WORKLOAD, now, Optional.empty(), "{}"), now);
        CloudCommandResult unsupportedRuntimeResult = getAgentCommandExecutionHandler()
                .execute(command(CloudCommandType.START_WORKLOAD, now, Optional.empty(), "{\"runtime\":\"kubernetes\"}"), now);
        CloudCommandResult missingSessionResult = getAgentCommandExecutionHandler()
                .execute(command(CloudCommandType.START_WORKLOAD, now, Optional.empty(), "{\"runtime\":\"tmux\",\"workloadId\":\"minecraft-kingdom-1\"}"), now);
        CloudCommandResult shellPayloadResult = getAgentCommandExecutionHandler()
                .execute(command(CloudCommandType.START_WORKLOAD, now, Optional.empty(), "{\"runtime\":\"raw_process\",\"workloadId\":\"dev\",\"processKey\":\"dev\",\"shellCommand\":\"rm -rf /\"}"), now);

        assertTrue(tmuxResult.success());
        assertTrue(tmuxResult.message().contains("Accepted tmux runtime command START_WORKLOAD."));
        assertFalse(missingRuntimeResult.success());
        assertTrue(missingRuntimeResult.errorSummary().contains("payload must declare runtime"));
        assertFalse(unsupportedRuntimeResult.success());
        assertTrue(unsupportedRuntimeResult.errorSummary().contains("Unsupported workload runtime adapter"));
        assertFalse(missingSessionResult.success());
        assertTrue(missingSessionResult.errorSummary().contains("sessionName"));
        assertFalse(shellPayloadResult.success());
        assertTrue(shellPayloadResult.errorSummary().contains("rejects raw shell payloads"));
    }

    @Test
    void agentRejectsUnsignedOrInvalidCommandsWhenSigningIsRequired() {
        DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(CloudAgentSecurityConfig.class, new CloudAgentSecurityConfig(true, "test-secret"));
        new CloudAgentDependencyModule().registerDependencies();
        Instant now = Instant.parse("2026-05-08T18:00:00Z");
        CloudCommand unsigned = command(CloudCommandType.RUN_HEALTH_CHECK, now, Optional.empty());
        CloudCommand invalid = signedCommand(unsigned, "not-valid");
        CloudCommand valid = signedCommand(unsigned, getCloudCommandSignatureHandler().signatureFor(unsigned));

        CloudCommandResult unsignedResult = getAgentCommandExecutionHandler().execute(unsigned, now);
        CloudCommandResult invalidResult = getAgentCommandExecutionHandler().execute(invalid, now);
        CloudCommandResult validResult = getAgentCommandExecutionHandler().execute(valid, now);

        assertFalse(unsignedResult.success());
        assertTrue(unsignedResult.errorSummary().contains("invalid signature"));
        assertFalse(invalidResult.success());
        assertTrue(invalidResult.errorSummary().contains("invalid signature"));
        assertTrue(validResult.success());
    }

    private CloudCommand command(CloudCommandType commandType, Instant requestedAt, Optional<Instant> expiresAt) {
        return command(commandType, requestedAt, expiresAt, "{}");
    }

    private CloudCommand command(CloudCommandType commandType, Instant requestedAt, Optional<Instant> expiresAt, String payloadJson) {
        return new CloudCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                commandType,
                payloadJson,
                UUID.randomUUID(),
                requestedAt,
                CloudCommandStatus.CREATED,
                Optional.empty(),
                UUID.randomUUID(),
                Optional.empty(),
                expiresAt,
                Map.of()
        );
    }

    private CloudCommand signedCommand(CloudCommand command, String signature) {
        return new CloudCommand(
                command.commandId(),
                command.nodeId(),
                command.commandType(),
                command.payloadJson(),
                command.requestedBy(),
                command.requestedAt(),
                command.status(),
                command.result(),
                command.correlationId(),
                Optional.of(signature),
                command.expiresAt(),
                command.metadata()
        );
    }
}
