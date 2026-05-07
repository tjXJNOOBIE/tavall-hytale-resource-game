package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduledControlCommandRepository {
    ScheduledControlCommand saveScheduledCommand(ScheduledControlCommand scheduledCommand);

    Optional<ScheduledControlCommand> findScheduledCommand(UUID scheduleId);

    List<ScheduledControlCommand> findDueCommands(Instant now, int limit);
}
