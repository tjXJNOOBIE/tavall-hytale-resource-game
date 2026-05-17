package com.tavall.resourcegame.api.internal.frontend.transport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.api.internal.permissions.RankResponse;
import com.tavall.resourcegame.api.internal.player.PlayerDataResponse;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FrontendTcpControlBridgeResponse(
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
    public FrontendTcpControlBridgeResponse {
        if (verificationResult == null && interactionResult == null && playerDataResponse == null && rankResponse == null && punishResponse == null && (errorMessage == null || errorMessage.isBlank())) {
            throw new IllegalArgumentException("verificationResult, interactionResult, playerDataResponse, rankResponse, punishResponse or errorMessage is required");
        }
    }

    public static FrontendTcpControlBridgeResponse success(FrontendCommandVerificationResult verificationResult) {
        return new FrontendTcpControlBridgeResponse(Objects.requireNonNull(verificationResult, "verificationResult"), null, null, null, null, null);
    }

    public static FrontendTcpControlBridgeResponse success(InteractionResult interactionResult) {
        return new FrontendTcpControlBridgeResponse(null, Objects.requireNonNull(interactionResult, "interactionResult"), null, null, null, null);
    }

    public static FrontendTcpControlBridgeResponse success(PlayerDataResponse playerDataResponse) {
        return new FrontendTcpControlBridgeResponse(null, null, Objects.requireNonNull(playerDataResponse, "playerDataResponse"), null, null, null);
    }

    public static FrontendTcpControlBridgeResponse success(RankResponse rankResponse) {
        return new FrontendTcpControlBridgeResponse(null, null, null, Objects.requireNonNull(rankResponse, "rankResponse"), null, null);
    }

    public static FrontendTcpControlBridgeResponse success(PunishResponse punishResponse) {
        return new FrontendTcpControlBridgeResponse(null, null, null, null, Objects.requireNonNull(punishResponse, "punishResponse"), null);
    }

    public static FrontendTcpControlBridgeResponse error(String errorMessage) {
        return new FrontendTcpControlBridgeResponse(null, null, null, null, null, Objects.requireNonNull(errorMessage, "errorMessage"));
    }
}
