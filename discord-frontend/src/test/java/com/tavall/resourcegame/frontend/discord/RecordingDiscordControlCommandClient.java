package com.tavall.resourcegame.frontend.discord;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Objects;
import java.util.function.Function;

final class RecordingDiscordControlCommandClient implements IDiscordControlCommandClient, IDependencyInjectableConcrete {
    private final Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler;

    RecordingDiscordControlCommandClient(Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler) {
        this.responseHandler = Objects.requireNonNull(responseHandler, "responseHandler");
    }

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return responseHandler.apply(envelope);
    }
}
