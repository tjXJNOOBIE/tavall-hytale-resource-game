package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.authority.AuthorizationResult;

import java.time.Instant;
import java.util.List;

public final class ControlCommandDispatchHandler implements ControlCommandDomain, IDependencyInjectableConcrete {
    public ControlCommandResult dispatchCommand(ControlCommand command) {
        Instant startedAt = Instant.now();
        List<String> validationErrors = getControlCommandValidationHandler().validateCommand(command);
        if (!validationErrors.isEmpty()) {
            ControlCommandResult rejectedResult = new ControlCommandResult(
                    command.commandId(),
                    CommandExecutionState.REJECTED,
                    false,
                    String.join(" ", validationErrors),
                    List.of(),
                    List.of(),
                    validationErrors,
                    startedAt,
                    Instant.now(),
                    java.util.Map.of()
            );
            getControlCommandResultHandler().recordResult(rejectedResult);
            getControlCommandAuditLogHandler().logCompletedCommand(command, rejectedResult, rejectedResult.completedAt());
            return rejectedResult;
        }
        if (getOptionalControlAuthorizationHandler().isPresent()) {
            AuthorizationResult authorizationResult = getOptionalControlAuthorizationHandler().orElseThrow().authorizeControlCommand(command);
            if (!authorizationResult.allowed()) {
                ControlCommandResult deniedResult = new ControlCommandResult(
                        command.commandId(),
                        CommandExecutionState.REJECTED,
                        false,
                        authorizationResult.message(),
                        List.of(),
                        List.of(),
                        List.of(authorizationResult.message()),
                        startedAt,
                        Instant.now(),
                        java.util.Map.of("authorization", "denied")
                );
                getControlCommandResultHandler().recordResult(deniedResult);
                getControlCommandAuditLogHandler().logCompletedCommand(command, deniedResult, deniedResult.completedAt());
                return deniedResult;
            }
        }

        ControlCommandResult canonicalResult = getControlCommandExecutionHandler().executeCommand(command, startedAt);
        ControlCommandResult finalResult = canonicalResult;
        ControlCommandDefinition definition = getControlCommandRegistry().definition(command.commandType());
        if (definition.fanout() && canonicalResult.success() && !command.dryRun()) {
            List<PlatformCommandResult> platformResults = getPlatformCommandFanoutHandler().fanoutCommand(command, canonicalResult.changedObjectIds());
            CommandExecutionState fanoutState = getPlatformFanoutResultAggregator().aggregateState(canonicalResult, platformResults);
            finalResult = new ControlCommandResult(
                    canonicalResult.commandId(),
                    fanoutState,
                    fanoutState == CommandExecutionState.COMPLETED,
                    canonicalResult.message(),
                    platformResults,
                    canonicalResult.changedObjectIds(),
                    canonicalResult.validationErrors(),
                    canonicalResult.startedAt(),
                    Instant.now(),
                    canonicalResult.metadata()
            );
        }
        getControlCommandResultHandler().recordResult(finalResult);
        getControlCommandAuditLogHandler().logCompletedCommand(command, finalResult, finalResult.completedAt());
        return finalResult;
    }
}
