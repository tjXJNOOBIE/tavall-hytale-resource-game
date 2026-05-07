package com.tavall.hytale.resourcegame.controlserver;

import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
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

        assertEquals("tavall-resource-game-control-server", module.moduleName());
        assertEquals("FrontendCommandIngressHandler", module.canonicalCommandIngressEntryPoint());
        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }
}
