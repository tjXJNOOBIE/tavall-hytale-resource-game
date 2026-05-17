package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;

import java.util.Objects;
import java.util.function.Function;

final class RecordingRobloxControlCommandClient implements IRobloxControlCommandClient, IDependencyInjectableConcrete {
    private final Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler;

    RecordingRobloxControlCommandClient(Function<FrontendCommandEnvelope, FrontendCommandVerificationResult> responseHandler) {
        this.responseHandler = Objects.requireNonNull(responseHandler, "responseHandler");
    }

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return responseHandler.apply(envelope);
    }
}
