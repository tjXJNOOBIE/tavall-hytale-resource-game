package com.tavall.resourcegame.services;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.api.internal.frontend.IFrontendControlCommandClient;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;

import java.time.Instant;
import java.util.Objects;

final class RuntimeBackedFrontendControlCommandClient implements IFrontendControlCommandClient, IDependencyInjectableConcrete {
    private final ControlCommandRuntime runtime;

    RuntimeBackedFrontendControlCommandClient(ControlCommandRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    public FrontendCommandVerificationResult submitCommand(FrontendCommandEnvelope envelope) {
        return runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());
    }
}
