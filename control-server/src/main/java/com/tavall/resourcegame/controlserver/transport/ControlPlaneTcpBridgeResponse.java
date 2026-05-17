package com.tavall.resourcegame.controlserver.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.InteractionResult;
import com.tavall.resourcegame.shared.frontend.PlayerDataResponse;
import com.tavall.resourcegame.shared.frontend.PunishResponse;
import com.tavall.resourcegame.shared.frontend.RankResponse;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ControlPlaneTcpBridgeResponse(
        @JsonProperty("verificationResult")
        FrontendCommandVerificationResult verificationResult,
        @JsonProperty("interactionResult")
        InteractionResult interactionResult,
        @JsonProperty("playerDataResponse")
        PlayerDataResponse playerDataResponse,
        @JsonProperty("rankResponse")
        RankResponse rankResponse,
        @JsonProperty("punishResponse")
        PunishResponse punishResponse,
        @JsonProperty("errorMessage")
        String errorMessage
) {
    public ControlPlaneTcpBridgeResponse {
        if (verificationResult == null && interactionResult == null && playerDataResponse == null && rankResponse == null && punishResponse == null && (errorMessage == null || errorMessage.isBlank())) {
            throw new IllegalArgumentException("verificationResult, interactionResult, playerDataResponse, rankResponse, punishResponse or errorMessage is required");
        }
    }

    public static ControlPlaneTcpBridgeResponse success(FrontendCommandVerificationResult verificationResult) {
        return new ControlPlaneTcpBridgeResponse(Objects.requireNonNull(verificationResult, "verificationResult"), null, null, null, null, null);
    }

    public static ControlPlaneTcpBridgeResponse success(InteractionResult interactionResult) {
        return new ControlPlaneTcpBridgeResponse(null, Objects.requireNonNull(interactionResult, "interactionResult"), null, null, null, null);
    }

    public static ControlPlaneTcpBridgeResponse success(PlayerDataResponse playerDataResponse) {
        return new ControlPlaneTcpBridgeResponse(null, null, Objects.requireNonNull(playerDataResponse, "playerDataResponse"), null, null, null);
    }

    public static ControlPlaneTcpBridgeResponse success(RankResponse rankResponse) {
        return new ControlPlaneTcpBridgeResponse(null, null, null, Objects.requireNonNull(rankResponse, "rankResponse"), null, null);
    }

    public static ControlPlaneTcpBridgeResponse success(PunishResponse punishResponse) {
        return new ControlPlaneTcpBridgeResponse(null, null, null, null, Objects.requireNonNull(punishResponse, "punishResponse"), null);
    }

    public static ControlPlaneTcpBridgeResponse error(String errorMessage) {
        return new ControlPlaneTcpBridgeResponse(null, null, null, null, null, Objects.requireNonNull(errorMessage, "errorMessage"));
    }
}
