package org.tavall.control;

import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationState;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
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
