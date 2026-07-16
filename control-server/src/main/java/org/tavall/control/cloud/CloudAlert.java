package org.tavall.control.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record CloudAlert(
        UUID alertId,
        CloudAlertType alertType,
        CloudAlertSeverity severity,
        String targetType,
        String targetId,
        String message,
        CloudAlertState state,
        Instant createdAt,
        Optional<Instant> resolvedAt,
        Map<String, String> metadata
) {
    public CloudAlert {
        resolvedAt = resolvedAt == null ? Optional.empty() : resolvedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
