package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.middleware.common.MetadataMaps;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ScheduledControlCommand(
        UUID scheduleId,
        ControlCommand command,
        Instant runAt,
        ScheduledControlCommandState state,
        Instant createdAt,
        Optional<Instant> dispatchedAt,
        Map<String, String> metadata
) {
    public ScheduledControlCommand {
        Objects.requireNonNull(scheduleId, "scheduleId");
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(runAt, "runAt");
        state = state == null ? ScheduledControlCommandState.PENDING : state;
        Objects.requireNonNull(createdAt, "createdAt");
        dispatchedAt = dispatchedAt == null ? Optional.empty() : dispatchedAt;
        metadata = MetadataMaps.immutable(metadata);
    }

    public ScheduledControlCommand dispatched(Instant now, ControlCommandResult result) {
        return new ScheduledControlCommand(
                scheduleId,
                command,
                runAt,
                result.success() ? ScheduledControlCommandState.DISPATCHED : ScheduledControlCommandState.FAILED,
                createdAt,
                Optional.of(now),
                Map.of("resultState", result.state().name(), "success", Boolean.toString(result.success()))
        );
    }
}
