package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.middleware.authority.AuthorizationResult;
import com.tavall.hytale.resourcegame.middleware.authority.IControlAuthorizationHandler;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public final class ControlCommandDispatchHandler {
    private final ControlCommandRegistry commandRegistry;
    private final ControlCommandValidationHandler validationHandler;
    private final ControlCommandExecutionHandler executionHandler;
    private final PlatformCommandFanoutHandler fanoutHandler;
    private final PlatformFanoutResultAggregator fanoutResultAggregator;
    private final ControlCommandResultHandler resultHandler;
    private final ControlCommandAuditLogHandler auditLogHandler;
    private final Optional<IControlAuthorizationHandler> authorizationHandler;

    public ControlCommandDispatchHandler(
            ControlCommandRegistry commandRegistry,
            ControlCommandValidationHandler validationHandler,
            ControlCommandExecutionHandler executionHandler,
            PlatformCommandFanoutHandler fanoutHandler,
            PlatformFanoutResultAggregator fanoutResultAggregator,
            ControlCommandResultHandler resultHandler,
            ControlCommandAuditLogHandler auditLogHandler
    ) {
        this(commandRegistry, validationHandler, executionHandler, fanoutHandler, fanoutResultAggregator, resultHandler, auditLogHandler, null);
    }

    public ControlCommandDispatchHandler(
            ControlCommandRegistry commandRegistry,
            ControlCommandValidationHandler validationHandler,
            ControlCommandExecutionHandler executionHandler,
            PlatformCommandFanoutHandler fanoutHandler,
            PlatformFanoutResultAggregator fanoutResultAggregator,
            ControlCommandResultHandler resultHandler,
            ControlCommandAuditLogHandler auditLogHandler,
            IControlAuthorizationHandler authorizationHandler
    ) {
        this.commandRegistry = commandRegistry;
        this.validationHandler = validationHandler;
        this.executionHandler = executionHandler;
        this.fanoutHandler = fanoutHandler;
        this.fanoutResultAggregator = fanoutResultAggregator;
        this.resultHandler = resultHandler;
        this.auditLogHandler = auditLogHandler;
        this.authorizationHandler = Optional.ofNullable(authorizationHandler);
    }

    public ControlCommandResult dispatchCommand(ControlCommand command) {
        Instant startedAt = Instant.now();
        List<String> validationErrors = validationHandler.validateCommand(command);
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
            resultHandler.recordResult(rejectedResult);
            auditLogHandler.logCompletedCommand(command, rejectedResult, rejectedResult.completedAt());
            return rejectedResult;
        }
        if (authorizationHandler.isPresent()) {
            AuthorizationResult authorizationResult = authorizationHandler.get().authorizeControlCommand(command);
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
                resultHandler.recordResult(deniedResult);
                auditLogHandler.logCompletedCommand(command, deniedResult, deniedResult.completedAt());
                return deniedResult;
            }
        }

        ControlCommandResult canonicalResult = executionHandler.executeCommand(command, startedAt);
        ControlCommandResult finalResult = canonicalResult;
        ControlCommandDefinition definition = commandRegistry.definition(command.commandType());
        if (definition.fanout() && canonicalResult.success() && !command.dryRun()) {
            List<PlatformCommandResult> platformResults = fanoutHandler.fanoutCommand(command, canonicalResult.changedObjectIds());
            CommandExecutionState fanoutState = fanoutResultAggregator.aggregateState(canonicalResult, platformResults);
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
        resultHandler.recordResult(finalResult);
        auditLogHandler.logCompletedCommand(command, finalResult, finalResult.completedAt());
        return finalResult;
    }
}
