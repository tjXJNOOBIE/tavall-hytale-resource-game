package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;

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
