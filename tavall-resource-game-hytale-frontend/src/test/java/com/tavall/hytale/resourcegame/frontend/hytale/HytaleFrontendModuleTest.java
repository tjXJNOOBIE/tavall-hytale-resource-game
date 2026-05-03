package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class HytaleFrontendModuleTest {
    @Test
    void hytaleFrontendIsAThinPlatformAdapter() {
        HytaleFrontendModule module = new HytaleFrontendModule();

        assertEquals("tavall-resource-game-hytale-frontend", module.moduleName());
        assertEquals("HYTALE", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.HYTALE, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.HYTALE_NATIVE_JAVA, module.descriptor().runtime());
        assertEquals("FrontendCommandIngressHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }

    @Test
    void hytaleCommandEnvelopeTargetsControlIngress() {
        HytaleFrontendCommandEnvelopeFactory factory = new HytaleFrontendCommandEnvelopeFactory();

        FrontendCommandEnvelope envelope = factory.commandEnvelope(
                "hytale-player",
                "Builder",
                "/kd ui castle-main",
                "corr-hytale",
                Map.of("server", "hytale-dev")
        );

        assertEquals(ResourceGameFrontendPlatform.HYTALE, envelope.platform());
        assertEquals("/kd ui castle-main", envelope.rawInput());
    }

    @Test
    void kdBridgePreservesNativeCommandAsControlEnvelope() {
        HytaleKdCommandEnvelopeBridge bridge = new HytaleKdCommandEnvelopeBridge();

        FrontendCommandEnvelope envelope = bridge.commandEnvelope(
                "hytale-player",
                "Builder",
                java.util.List.of("resources", "add", "food", "10"),
                "corr-kd-resources",
                Map.of("server", "hytale-dev")
        );

        assertEquals(ResourceGameFrontendPlatform.HYTALE, envelope.platform());
        assertEquals("/kd resources add food 10", envelope.rawInput());
        assertEquals("hytale-dev", envelope.sourceMetadata().get("server"));
    }

    @Test
    void hytaleControlPlaneBridgeSubmitsKdCommandToClient() {
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        HytaleControlPlaneCommandBridge bridge = new HytaleControlPlaneCommandBridge(
                new HytaleKdCommandEnvelopeBridge(),
                envelope -> {
                    submittedEnvelope.set(envelope);
                    return new FrontendCommandVerificationResult(
                            envelope,
                            FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED,
                            true,
                            "verified",
                            "cmd-hytale",
                            "COMPLETED",
                            Map.of("verifiedCategory", "ui")
                    );
                }
        );

        FrontendCommandVerificationResult result = bridge.submitKdCommand(
                "hytale-player",
                "Builder",
                List.of("ui", "castle-main"),
                "corr-hytale-submit",
                Map.of("server", "hytale-dev")
        );

        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
        assertEquals("/kd ui castle-main", submittedEnvelope.get().rawInput());
        assertEquals(ResourceGameFrontendPlatform.HYTALE, submittedEnvelope.get().platform());
    }
}
