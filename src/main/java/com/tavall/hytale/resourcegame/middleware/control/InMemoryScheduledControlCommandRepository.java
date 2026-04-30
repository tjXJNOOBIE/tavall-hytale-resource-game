package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryScheduledControlCommandRepository implements ScheduledControlCommandRepository {
    private final Map<UUID, ScheduledControlCommand> scheduledCommandsById = new ConcurrentHashMap<>();

    @Override
    public ScheduledControlCommand saveScheduledCommand(ScheduledControlCommand scheduledCommand) {
        scheduledCommandsById.put(scheduledCommand.scheduleId(), scheduledCommand);
        return scheduledCommand;
    }

    @Override
    public Optional<ScheduledControlCommand> findScheduledCommand(UUID scheduleId) {
        return Optional.ofNullable(scheduledCommandsById.get(scheduleId));
    }

    @Override
    public List<ScheduledControlCommand> findDueCommands(Instant now, int limit) {
        return scheduledCommandsById.values().stream()
                .filter(scheduledCommand -> scheduledCommand.state() == ScheduledControlCommandState.PENDING)
                .filter(scheduledCommand -> !scheduledCommand.runAt().isAfter(now))
                .sorted(Comparator.comparing(ScheduledControlCommand::runAt))
                .limit(Math.max(0, limit))
                .toList();
    }
}
