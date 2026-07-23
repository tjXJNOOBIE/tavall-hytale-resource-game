package org.tavall.control.runtime;

import org.tavall.dependency.IDependencyInjectableConcrete;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ControlCommandSchedulingHandler implements ControlCommandDomain, IDependencyInjectableConcrete {
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
        return getScheduledControlCommandRepository().saveScheduledCommand(scheduledCommand);
    }

    public List<ControlCommandResult> dispatchDueCommands(Instant now, int limit) {
        return getScheduledControlCommandRepository().findDueCommands(now, limit).stream()
                .map(scheduledCommand -> dispatchScheduledCommand(scheduledCommand, now))
                .toList();
    }

    private ControlCommandResult dispatchScheduledCommand(ScheduledControlCommand scheduledCommand, Instant now) {
        ControlCommandResult result = getControlCommandDispatchHandler().dispatchCommand(scheduledCommand.command());
        getScheduledControlCommandRepository().saveScheduledCommand(scheduledCommand.dispatched(now, result));
        return result;
    }
}
