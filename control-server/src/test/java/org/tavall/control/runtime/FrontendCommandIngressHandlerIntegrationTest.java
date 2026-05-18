package org.tavall.control.runtime;

import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandSurface;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationState;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendCommandIngressHandlerIntegrationTest {

    @Test
    void minecraftKdTickCommandDispatchesThroughCanonicalControlPipeline() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                "minecraft-player-1",
                "Healer",
                "/kd tick run 1",
                "corr-kd-tick",
                Map.of("worldId", "minecraft-dev")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("tick healing 1", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void minecraftLegacyKdCategoryDispatchesToBackendRouteBeforeAdapterHandlesIt() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                "minecraft-player-2",
                "Builder",
                "/kd ui castle-main",
                "corr-kd-ui",
                Map.of("surface", "command")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, Instant.now());

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("ROUTE_FRONTEND_KD_COMMAND", result.metadata().get("commandType"));
        assertEquals("frontend kd ui kd ui castle-main", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void instanceSwitchCommandReturnsSwitchMetadataForFrontendDispatch() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        Instant now = Instant.parse("2026-05-06T16:00:00Z");
        runtime.kingdomSimulationSystem().createKingdom("First", "default", 1000, now);
        runtime.kingdomSimulationSystem().createKingdom("Second", "default", 1000, now.plusSeconds(1));
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                "minecraft-player-1",
                "ProxyAdmin",
                "/kd instance switch player-1 minecraft kingdom-1 kingdom-2",
                "corr-instance-switch",
                Map.of("server", "lobby")
        );

        FrontendCommandVerificationResult result = runtime.frontendCommandIngressHandler().ingest(envelope, now.plusSeconds(2));

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("REQUEST_INSTANCE_SWITCH", result.metadata().get("commandType"));
        assertTrue(result.metadata().containsKey("instanceSwitchRequestId"));
        assertEquals("minecraft", result.metadata().get("platform"));
        assertEquals("kingdom-2", result.metadata().get("toKingdomId"));
        assertTrue(result.metadata().get("changedObjectIds").contains("platform-instance:kingdom-2-minecraft-primary"));
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
