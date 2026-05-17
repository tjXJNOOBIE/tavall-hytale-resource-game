package com.tavall.resourcegame.services;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlCommandClient;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;

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
