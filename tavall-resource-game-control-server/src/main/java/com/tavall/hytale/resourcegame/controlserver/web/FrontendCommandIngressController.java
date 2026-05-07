package com.tavall.hytale.resourcegame.controlserver.web;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class FrontendCommandIngressController {
    private final ControlCommandRuntime runtime;

    public FrontendCommandIngressController(ControlCommandRuntime runtime) {
        this.runtime = runtime;
    }

    @PostMapping("/api/frontend/commands")
    public FrontendCommandVerificationResult ingestFrontendCommand(@RequestBody FrontendCommandEnvelope envelope) {
        return runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());
    }
}
