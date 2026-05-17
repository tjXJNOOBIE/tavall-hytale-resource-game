package com.tavall.resourcegame.api.internal.frontend;

import com.tavall.resourcegame.api.internal.interaction.InteractionRequest;
import com.tavall.resourcegame.api.internal.interaction.InteractionResult;
import com.tavall.resourcegame.api.internal.interaction.InteractionResultType;
import com.tavall.resourcegame.api.internal.permissions.PunishRequest;
import com.tavall.resourcegame.api.internal.permissions.PunishResponse;
import com.tavall.resourcegame.api.internal.permissions.RankRequest;
import com.tavall.resourcegame.api.internal.permissions.RankResponse;
import com.tavall.resourcegame.api.internal.player.PlayerDataRequest;
import com.tavall.resourcegame.api.internal.player.PlayerDataResponse;

import java.util.Map;

@FunctionalInterface
public interface FrontendControlCommandClient {
    FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope);

    default InteractionResult submitInteraction(InteractionRequest request) {
        return new InteractionResult(
                request.requestId(),
                InteractionResultType.BACKEND_UNAVAILABLE,
                false,
                "Interaction bridge is not available.",
                null,
                "Backend unavailable.",
                Map.of("requestType", request.interactionType())
        );
    }

    default PlayerDataResponse fetchPlayerData(PlayerDataRequest request) {
        return PlayerDataResponse.unavailable(request, "Player data bridge is not available.");
    }

    default RankResponse submitRankRequest(RankRequest request) {
        return RankResponse.unavailable(request == null ? "rank-unavailable" : request.requestId(), "Rank bridge is not available.");
    }

    default PunishResponse submitPunishRequest(PunishRequest request) {
        return PunishResponse.unavailable(request == null ? "punish-unavailable" : request.requestId(), "Punishment bridge is not available.");
    }
}
