package org.tavall.api.minecraft.frontend;

import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionResultType;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;

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
