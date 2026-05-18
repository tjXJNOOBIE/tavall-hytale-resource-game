package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record CloudCommandResult(
        UUID commandId,
        UUID nodeId,
        boolean success,
        Optional<Integer> exitCode,
        String message,
        Instant startedAt,
        Instant completedAt,
        String outputSummary,
        String errorSummary,
        Map<String, String> metadata
) {
    public CloudCommandResult {
        exitCode = exitCode == null ? Optional.empty() : exitCode;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
