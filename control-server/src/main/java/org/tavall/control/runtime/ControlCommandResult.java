package org.tavall.control.runtime;

import org.tavall.control.common.MetadataMaps;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record ControlCommandResult(
        ControlCommandId commandId,
        CommandExecutionState state,
        boolean success,
        String message,
        List<PlatformCommandResult> platformResults,
        List<String> changedObjectIds,
        List<String> validationErrors,
        Instant startedAt,
        Instant completedAt,
        Map<String, String> metadata
) {
    public ControlCommandResult {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(state, "state");
        message = message == null ? "" : message;
        platformResults = platformResults == null ? List.of() : List.copyOf(platformResults);
        changedObjectIds = changedObjectIds == null ? List.of() : List.copyOf(changedObjectIds);
        validationErrors = validationErrors == null ? List.of() : List.copyOf(validationErrors);
        Objects.requireNonNull(startedAt, "startedAt");
        Objects.requireNonNull(completedAt, "completedAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public ControlCommandResult withPlatformResults(List<PlatformCommandResult> platformResults, CommandExecutionState state) {
        boolean fanoutSuccess = platformResults == null || platformResults.stream().allMatch(PlatformCommandResult::success);
        return new ControlCommandResult(commandId, state, success && fanoutSuccess, message, platformResults, changedObjectIds, validationErrors, startedAt, completedAt, metadata);
    }
}
