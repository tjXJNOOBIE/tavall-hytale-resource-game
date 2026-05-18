package org.tavall.minecraft;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.permissions.PunishRequest;
import org.tavall.api.minecraft.permissions.PunishResponse;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.minecraft.bridge.IMinecraftControlCommandClient;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Objects;
import java.util.function.Function;

final class RecordingMinecraftControlCommandClient implements IMinecraftControlCommandClient, IDependencyInjectableConcrete {
    private final Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler;
    private final Function<RankRequest, RankResponse> rankResponseHandler;
    private final Function<PunishRequest, PunishResponse> punishResponseHandler;

    RecordingMinecraftControlCommandClient(Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler) {
        this(responseHandler, request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."), request -> PunishResponse.unavailable(request.requestId(), "Punish request not mocked."));
    }

    RecordingMinecraftControlCommandClient(
            Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler,
            Function<RankRequest, RankResponse> rankResponseHandler
    ) {
        this(responseHandler, rankResponseHandler, request -> PunishResponse.unavailable(request.requestId(), "Punish request not mocked."));
    }

    RecordingMinecraftControlCommandClient(
            Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler,
            Function<RankRequest, RankResponse> rankResponseHandler,
            Function<PunishRequest, PunishResponse> punishResponseHandler
    ) {
        this.responseHandler = Objects.requireNonNull(responseHandler, "responseHandler");
        this.rankResponseHandler = Objects.requireNonNull(rankResponseHandler, "rankResponseHandler");
        this.punishResponseHandler = Objects.requireNonNull(punishResponseHandler, "punishResponseHandler");
    }

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return responseHandler.apply(envelope);
    }

    @Override
    public RankResponse submitRankRequest(RankRequest request) {
        return rankResponseHandler.apply(request);
    }

    @Override
    public PunishResponse submitPunishRequest(PunishRequest request) {
        return punishResponseHandler.apply(request);
    }
}
