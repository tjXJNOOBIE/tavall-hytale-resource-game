package com.tavall.hytale.resourcegame.middleware.control;

import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendCommandIngressHandlerIntegrationTest {

    @Test
    void kdTickCommandDispatchesThroughCanonicalControlPipeline() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.HYTALE,
                "hytale-player-1",
                "Healer",
                "/kd tick run 1",
                "corr-kd-tick",
                Map.of("worldId", "hytale-dev")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("tick healing 1", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void hytaleLocalKdCategoryIsVerifiedAndAuditedBeforeAdapterHandlesIt() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.HYTALE,
                "hytale-player-2",
                "Builder",
                "/kd ui castle-main",
                "corr-kd-ui",
                Map.of("surface", "command")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
        assertTrue(result.success());
        assertEquals("ui", result.metadata().get("verifiedCategory"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void androidAndPcFrontendsCanSubmitThroughControlIngress() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandEnvelope pcEnvelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.PC,
                "pc-player-1",
                "FutureClient",
                "params list",
                "corr-pc",
                Map.of()
        );
        FrontendCommandEnvelope androidEnvelope = FrontendCommandEnvelope.action(
                ResourceGameFrontendPlatform.ANDROID,
                FrontendCommandSurface.UI_ACTION,
                "android-player-1",
                "MobileClient",
                "android.kingdom.summary",
                Map.of("kingdomId", "kingdom-1"),
                "corr-android",
                Map.of()
        );

        FrontendCommandVerificationResult pcResult = runtime.frontendCommandIngressHandler().ingest(pcEnvelope, Instant.now());
        FrontendCommandVerificationResult androidResult = runtime.frontendCommandIngressHandler().ingest(androidEnvelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, pcResult.state());
        assertTrue(pcResult.success());
        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, androidResult.state());
        assertTrue(androidResult.success());
    }
}
