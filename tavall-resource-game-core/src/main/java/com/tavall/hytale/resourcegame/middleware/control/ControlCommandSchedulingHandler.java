package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ControlCommandSchedulingHandler {
    private final ScheduledControlCommandRepository scheduledCommandRepository;
    private final ControlCommandDispatchHandler dispatchHandler;

    public ControlCommandSchedulingHandler(ScheduledControlCommandRepository scheduledCommandRepository, ControlCommandDispatchHandler dispatchHandler) {
        this.scheduledCommandRepository = scheduledCommandRepository;
        this.dispatchHandler = dispatchHandler;
    }

    public ScheduledControlCommand scheduleCommand(ControlCommand command, Instant runAt, Instant now) {
        ScheduledControlCommand scheduledCommand = new ScheduledControlCommand(
                UUID.randomUUID(),
                command,
                runAt,
                ScheduledControlCommandState.PENDING,
                now,
                java.util.Optional.empty(),
                Map.of()
        );
        return scheduledCommandRepository.saveScheduledCommand(scheduledCommand);
    }

    public List<ControlCommandResult> dispatchDueCommands(Instant now, int limit) {
        return scheduledCommandRepository.findDueCommands(now, limit).stream()
                .map(scheduledCommand -> dispatchScheduledCommand(scheduledCommand, now))
                .toList();
    }

    private ControlCommandResult dispatchScheduledCommand(ScheduledControlCommand scheduledCommand, Instant now) {
        ControlCommandResult result = dispatchHandler.dispatchCommand(scheduledCommand.command());
        scheduledCommandRepository.saveScheduledCommand(scheduledCommand.dispatched(now, result));
        return result;
    }
}
