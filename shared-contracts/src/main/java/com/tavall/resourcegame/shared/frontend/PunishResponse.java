package com.tavall.resourcegame.shared.frontend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PunishResponse(
        @JsonProperty("requestId")
        String requestId,
        @JsonProperty("success")
        boolean success,
        @JsonProperty("message")
        String message,
        @JsonProperty("activePunishment")
        PunishRecord activePunishment,
        @JsonProperty("punishments")
        List<PunishRecord> punishments,
        @JsonProperty("metadata")
        Map<String, String> metadata
) {
    @JsonCreator
    public PunishResponse {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(message, "message");
        punishments = punishments == null ? List.of() : List.copyOf(punishments);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static PunishResponse unavailable(String requestId, String message) {
        return new PunishResponse(
                requestId == null || requestId.isBlank() ? "punish-unavailable" : requestId,
                false,
                message == null || message.isBlank() ? "Punishment data is unavailable." : message,
                null,
                List.of(),
                Map.of()
        );
    }

    public static PunishResponse inspected(String requestId, String message, PunishRecord activePunishment, List<PunishRecord> punishments, Map<String, String> metadata) {
        return new PunishResponse(requestId, true, message, activePunishment, punishments, metadata);
    }

    public static PunishResponse updated(String requestId, String message, PunishRecord activePunishment, List<PunishRecord> punishments, Map<String, String> metadata) {
        return new PunishResponse(requestId, true, message, activePunishment, punishments, metadata);
    }
}
