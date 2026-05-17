package com.tavall.resourcegame.shared.frontend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record RankResponse(
        @JsonProperty("requestId")
        String requestId,
        @JsonProperty("success")
        boolean success,
        @JsonProperty("message")
        String message,
        @JsonProperty("subject")
        UniversalPermissionSubject subject,
        @JsonProperty("subjects")
        List<UniversalPermissionSubject> subjects,
        @JsonProperty("metadata")
        Map<String, String> metadata
) {
    @JsonCreator
    public RankResponse {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(message, "message");
        if (requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        subjects = subjects == null ? List.of() : List.copyOf(subjects);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static RankResponse unavailable(String requestId, String message) {
        return new RankResponse(
                requestId == null || requestId.isBlank() ? "rank-unavailable" : requestId,
                false,
                message == null || message.isBlank() ? "Rank data is unavailable." : message,
                null,
                List.of(),
                Map.of()
        );
    }

    public static RankResponse listed(String requestId, String message, List<UniversalPermissionSubject> subjects, Map<String, String> metadata) {
        return new RankResponse(
                requestId,
                true,
                message,
                null,
                subjects,
                metadata
        );
    }

    public static RankResponse inspected(String requestId, String message, UniversalPermissionSubject subject, Map<String, String> metadata) {
        return new RankResponse(
                requestId,
                true,
                message,
                subject,
                List.of(),
                metadata
        );
    }

    public static RankResponse updated(String requestId, String message, UniversalPermissionSubject subject, List<UniversalPermissionSubject> subjects, Map<String, String> metadata) {
        return new RankResponse(
                requestId,
                true,
                message,
                subject,
                subjects,
                metadata
        );
    }
}
