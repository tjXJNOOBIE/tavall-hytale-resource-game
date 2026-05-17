package com.tavall.resourcegame.api.internal.frontend;

import java.util.Map;
import java.util.Objects;

public record FrontendCommandVerificationResult(
        FrontendCommandEnvelope envelope,
        FrontendCommandVerificationState state,
        boolean success,
        String message,
        String controlCommandId,
        String controlCommandState,
        Map<String, String> metadata
) {
    public FrontendCommandVerificationResult {
        Objects.requireNonNull(envelope, "envelope");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(message, "message");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static FrontendCommandVerificationResult rejected(FrontendCommandEnvelope envelope, String message) {
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.REJECTED,
                false,
                message,
                null,
                null,
                Map.of()
        );
    }
}
