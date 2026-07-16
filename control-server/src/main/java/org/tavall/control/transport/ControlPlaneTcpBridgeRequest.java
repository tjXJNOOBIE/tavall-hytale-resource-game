package org.tavall.control.transport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.player.PlayerDataRequest;

import java.util.Objects;

public record ControlPlaneTcpBridgeRequest(
        @JsonProperty("requestType")
        ControlPlaneTcpBridgeRequestType requestType,
        @JsonProperty("frontendCommand")
        FrontendCommandEnvelope frontendCommand,
        @JsonProperty("interactionRequest")
        InteractionRequest interactionRequest,
        @JsonProperty("playerDataRequest")
        PlayerDataRequest playerDataRequest,
        @JsonProperty("rankRequest")
        RankRequest rankRequest,
        @JsonProperty("punishRequest")
        PunishRequest punishRequest
) {
    @JsonCreator
    public ControlPlaneTcpBridgeRequest {
        Objects.requireNonNull(requestType, "requestType");
        if (requestType == ControlPlaneTcpBridgeRequestType.FRONTEND_COMMAND && frontendCommand == null) {
            throw new IllegalArgumentException("frontendCommand is required for command requests");
        }
        if (requestType == ControlPlaneTcpBridgeRequestType.INTERACTION_REQUEST && interactionRequest == null) {
            throw new IllegalArgumentException("interactionRequest is required for interaction requests");
        }
        if (requestType == ControlPlaneTcpBridgeRequestType.PLAYER_DATA_REQUEST && playerDataRequest == null) {
            throw new IllegalArgumentException("playerDataRequest is required for player data requests");
        }
        if (requestType == ControlPlaneTcpBridgeRequestType.RANK_REQUEST && rankRequest == null) {
            throw new IllegalArgumentException("rankRequest is required for rank requests");
        }
        if (requestType == ControlPlaneTcpBridgeRequestType.PUNISH_REQUEST && punishRequest == null) {
            throw new IllegalArgumentException("punishRequest is required for punish requests");
        }
    }

    public static ControlPlaneTcpBridgeRequest frontendCommand(FrontendCommandEnvelope envelope) {
        return new ControlPlaneTcpBridgeRequest(ControlPlaneTcpBridgeRequestType.FRONTEND_COMMAND, Objects.requireNonNull(envelope, "envelope"), null, null, null, null);
    }

    public static ControlPlaneTcpBridgeRequest interaction(InteractionRequest request) {
        return new ControlPlaneTcpBridgeRequest(ControlPlaneTcpBridgeRequestType.INTERACTION_REQUEST, null, Objects.requireNonNull(request, "request"), null, null, null);
    }

    public static ControlPlaneTcpBridgeRequest playerData(PlayerDataRequest request) {
        return new ControlPlaneTcpBridgeRequest(ControlPlaneTcpBridgeRequestType.PLAYER_DATA_REQUEST, null, null, Objects.requireNonNull(request, "request"), null, null);
    }

    public static ControlPlaneTcpBridgeRequest rank(RankRequest request) {
        return new ControlPlaneTcpBridgeRequest(ControlPlaneTcpBridgeRequestType.RANK_REQUEST, null, null, null, Objects.requireNonNull(request, "request"), null);
    }

    public static ControlPlaneTcpBridgeRequest punish(PunishRequest request) {
        return new ControlPlaneTcpBridgeRequest(ControlPlaneTcpBridgeRequestType.PUNISH_REQUEST, null, null, null, null, Objects.requireNonNull(request, "request"));
    }
}
