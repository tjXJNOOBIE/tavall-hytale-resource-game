package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendSurfaceIdentity;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tjxjnoobie.api.dependency.DependencyLoader;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HytaleFrontendModuleTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
    }

    @Test
    void hytaleFrontendIsAThinPlatformAdapter() {
        HytaleFrontendModule module = new HytaleFrontendModule();

        assertEquals("hytale-frontend", module.moduleName());
        assertEquals("HYTALE", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.HYTALE, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.HYTALE_NATIVE_JAVA, module.descriptor().runtime());
        assertEquals(ResourceGameFrontendSurfaceIdentity.HYTALE_SINGLE_SERVER, module.surfaceIdentity());
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
        new HytaleFrontendDependencyModule().registerDependencies();
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
        DependencyLoaderAccess.registerInstance(
                IHytaleControlCommandClient.class,
                new RecordingHytaleControlCommandClient(submittedEnvelope)
        );
        new HytaleFrontendDependencyModule().registerDependencies();
        HytaleControlPlaneCommandBridge bridge = new HytaleControlPlaneCommandBridge();

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

    @Test
    void hytaleConfigPrefersSpecificControlIngress() {
        HytaleFrontendConfig config = HytaleFrontendConfig.fromEnvironment(Map.of(
                "RESOURCE_GAME_HYTALE_CONTROL_INGRESS_URL", "tcp://127.0.0.1:18081",
                "RESOURCE_GAME_CONTROL_INGRESS_URL", "tcp://127.0.0.1:18081",
                "RESOURCE_GAME_HYTALE_SERVER_ID", "hytale-dev-1"
        ));

        assertEquals("tcp://127.0.0.1:18081", config.controlIngressUri().toString());
        assertEquals("hytale-dev-1", config.serverId());
    }

    @Test
    void hytaleConfigRejectsHttpControlIngress() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                HytaleFrontendConfig.fromEnvironment(Map.of(
                        "RESOURCE_GAME_HYTALE_CONTROL_INGRESS_URL", "http://127.0.0.1:18080"
                ))
        );

        assertTrue(exception.getMessage().contains("tcp://host:port"));
    }

    @Test
    void hytaleDependencyModuleRegistersTcpControlClient() {
        DependencyLoaderAccess.registerInstance(
                IHytaleFrontendConfig.class,
                new HytaleFrontendConfig(
                        URI.create("tcp://127.0.0.1:18081"),
                        "hytale-test"
                )
        );
        new HytaleFrontendDependencyModule().registerDependencies();

        assertTrue(DependencyLoaderAccess.findInstance(IHytaleControlCommandClient.class) instanceof HytaleTcpControlCommandClient);
    }
}
