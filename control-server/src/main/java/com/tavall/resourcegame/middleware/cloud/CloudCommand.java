package com.tavall.resourcegame.middleware.cloud;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record CloudCommand(
        UUID commandId,
        UUID nodeId,
        CloudCommandType commandType,
        String payloadJson,
        UUID requestedBy,
        Instant requestedAt,
        CloudCommandStatus status,
        Optional<String> result,
        UUID correlationId,
        Optional<String> signature,
        Optional<Instant> expiresAt,
        Map<String, String> metadata
) {
    public CloudCommand {
        result = result == null ? Optional.empty() : result;
        signature = signature == null ? Optional.empty() : signature;
        expiresAt = expiresAt == null ? Optional.empty() : expiresAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public CloudCommand withStatus(CloudCommandStatus nextStatus, Optional<String> nextResult) {
        return new CloudCommand(commandId, nodeId, commandType, payloadJson, requestedBy, requestedAt, nextStatus,
                nextResult, correlationId, signature, expiresAt, metadata);
    }
}
