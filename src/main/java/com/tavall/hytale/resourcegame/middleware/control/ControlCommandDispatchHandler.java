package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Instant;
import java.util.List;

public final class ControlCommandDispatchHandler {
    private final ControlCommandRegistry commandRegistry;
    private final ControlCommandValidationHandler validationHandler;
    private final ControlCommandExecutionHandler executionHandler;
    private final PlatformCommandFanoutHandler fanoutHandler;
    private final PlatformFanoutResultAggregator fanoutResultAggregator;
    private final ControlCommandResultHandler resultHandler;
    private final ControlCommandAuditLogHandler auditLogHandler;

    public ControlCommandDispatchHandler(
            ControlCommandRegistry commandRegistry,
            ControlCommandValidationHandler validationHandler,
            ControlCommandExecutionHandler executionHandler,
            PlatformCommandFanoutHandler fanoutHandler,
            PlatformFanoutResultAggregator fanoutResultAggregator,
            ControlCommandResultHandler resultHandler,
            ControlCommandAuditLogHandler auditLogHandler
    ) {
        this.commandRegistry = commandRegistry;
        this.validationHandler = validationHandler;
        this.executionHandler = executionHandler;
        this.fanoutHandler = fanoutHandler;
        this.fanoutResultAggregator = fanoutResultAggregator;
        this.resultHandler = resultHandler;
        this.auditLogHandler = auditLogHandler;
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
