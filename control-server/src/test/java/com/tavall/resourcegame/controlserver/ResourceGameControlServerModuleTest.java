package com.tavall.resourcegame.controlserver;

import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.api.internal.frontend.FrontendCommandVerificationState;
import com.tavall.resourcegame.api.internal.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ResourceGameControlServerModuleTest {
    @Test
    void controlServerOwnsCanonicalFrontendCommandIngress() {
        ResourceGameControlServerModule module = new ResourceGameControlServerModule();
        ControlCommandRuntime runtime = module.createInMemoryRuntime();

        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.HYTALE,
                "hytale-player",
                "Builder",
                "/kd ui castle-main",
                "corr-control-module",
                Map.of()
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals("control-server", module.moduleName());
        assertEquals("FrontendCommandIngressHandler", module.canonicalCommandIngressEntryPoint());
        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("frontend kd ui kd ui castle-main", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }
}
