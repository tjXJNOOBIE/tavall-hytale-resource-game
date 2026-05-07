package com.tavall.hytale.resourcegame.services;

import com.tavall.hytale.resourcegame.frontend.hytale.HytaleKdCommandEnvelopeBridge;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.hytale.resourcegame.middleware.control.ControlCommandRuntimeFactory;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendCommandVerificationServiceIntegrationTest {

    @Test
    void hytaleKdCommandIsVerifiedThroughControlIngress() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandVerificationService service = new FrontendCommandVerificationService(
                runtime,
                new HytaleKdCommandEnvelopeBridge()
        );

        FrontendCommandVerificationResult result = service.verifyHytaleKdCommand(
                "hytale-player-1",
                "Builder",
                List.of("ui", "castle-main"),
                Map.of("server", "hytale-dev")
        );

        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
        assertTrue(result.success());
        assertEquals("ui", result.metadata().get("verifiedCategory"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void translatedKdCommandDispatchesAndDoesNotNeedAdapterMutation() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandVerificationService service = new FrontendCommandVerificationService(
                runtime,
                new HytaleKdCommandEnvelopeBridge()
        );

        FrontendCommandVerificationResult result = service.verifyHytaleKdCommand(
                "hytale-player-2",
                "Healer",
                List.of("tick", "run", "2"),
                Map.of("server", "hytale-dev")
        );

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("tick healing 2", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }
}
