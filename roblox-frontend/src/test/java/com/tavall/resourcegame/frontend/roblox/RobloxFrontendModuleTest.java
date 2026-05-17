package com.tavall.resourcegame.frontend.roblox;

import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandSurface;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tjxjnoobie.api.dependency.DependencyLoader;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public final class RobloxFrontendModuleTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
    }

    @Test
    void robloxFrontendIsALuauAdapterBackedBySharedContracts() {
        RobloxFrontendModule module = new RobloxFrontendModule();

        assertEquals("roblox-frontend", module.moduleName());
        assertEquals("ROBLOX", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.ROBLOX, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.ROBLOX_LUAU, module.descriptor().runtime());
        assertEquals("FrontendCommandIngressHandler", module.descriptor().commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }

    @Test
    void robloxRemoteEventEnvelopeTargetsControlIngress() {
        RobloxFrontendCommandEnvelopeFactory factory = new RobloxFrontendCommandEnvelopeFactory();

        FrontendCommandEnvelope envelope = factory.actionEnvelope(
                FrontendCommandSurface.REMOTE_EVENT,
                "roblox-user",
                "Courier",
                "action.trade_route.inspect",
                Map.of("routeId", "route-1"),
                "corr-roblox",
                Map.of("placeId", "kingdoms")
        );

        assertEquals(ResourceGameFrontendPlatform.ROBLOX, envelope.platform());
        assertEquals("route-1", envelope.arguments().get("routeId"));
    }

    @Test
    void robloxKdBridgeCreatesRemoteCommandEnvelope() {
        registerRobloxDependencies(null);
        RobloxKdCommandEnvelopeBridge bridge = new RobloxKdCommandEnvelopeBridge();

        FrontendCommandEnvelope envelope = bridge.commandEnvelope(
                "roblox-user",
                "Courier",
                java.util.List.of("market", "listing", "inspect", "listing-1"),
                "corr-roblox-kd",
                Map.of("placeId", "kingdoms")
        );

        assertEquals(ResourceGameFrontendPlatform.ROBLOX, envelope.platform());
        assertEquals("/kd market listing inspect listing-1", envelope.rawInput());
        assertEquals("kingdoms", envelope.sourceMetadata().get("placeId"));
    }

    @Test
    void robloxControlPlaneBridgeSubmitsKdCommandToClient() {
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        registerRobloxDependencies(submittedEnvelope);
        RobloxControlPlaneCommandBridge bridge = new RobloxControlPlaneCommandBridge();

        FrontendCommandVerificationResult result = bridge.submitKdCommand(
                "roblox-user",
                "Courier",
                List.of("market", "listing", "inspect", "listing-1"),
                "corr-roblox-submit",
                Map.of("placeId", "kingdoms")
        );

        assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
        assertEquals("/kd market listing inspect listing-1", submittedEnvelope.get().rawInput());
        assertEquals(ResourceGameFrontendPlatform.ROBLOX, submittedEnvelope.get().platform());
    }

    private void registerRobloxDependencies(AtomicReference<FrontendCommandEnvelope> submittedEnvelope) {
        DependencyLoaderAccess.registerInstance(
                IRobloxControlCommandClient.class,
                new RecordingRobloxControlCommandClient(envelope -> {
                    submittedEnvelope.set(envelope);
                    return new FrontendCommandVerificationResult(
                            envelope,
                            FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED,
                            true,
                            "verified",
                            "cmd-roblox",
                            "COMPLETED",
                            Map.of("verifiedCategory", "market")
                    );
                })
        );
        new RobloxFrontendDependencyModule().registerDependencies(DependencyLoaderAccess.requireInstance(IRobloxControlCommandClient.class));
    }
}
