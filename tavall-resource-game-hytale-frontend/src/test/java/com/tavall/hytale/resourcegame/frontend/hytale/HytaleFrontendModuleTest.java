package com.tavall.hytale.resourcegame.frontend.hytale;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendSurfaceIdentity;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class HytaleFrontendModuleTest {
    @Test
    void hytaleFrontendIsAThinPlatformAdapter() {
        HytaleFrontendModule module = new HytaleFrontendModule();

        assertEquals("tavall-resource-game-hytale-frontend", module.moduleName());
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

    @Test
    void hytaleConfigPrefersSpecificControlIngress() {
        HytaleFrontendConfig config = HytaleFrontendConfig.fromEnvironment(Map.of(
                "RESOURCE_GAME_HYTALE_CONTROL_INGRESS_URL", "http://127.0.0.1:19080/api/frontend/commands",
                "RESOURCE_GAME_CONTROL_INGRESS_URL", "http://127.0.0.1:8080/api/frontend/commands",
                "RESOURCE_GAME_HYTALE_SERVER_ID", "hytale-dev-1"
        ));

        assertEquals("http://127.0.0.1:19080/api/frontend/commands", config.controlIngressUri().toString());
        assertEquals("hytale-dev-1", config.serverId());
    }

    @Test
    void hytaleHttpClientPostsEnvelopeToControlIngress() throws IOException {
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/frontend/commands", exchange -> {
            byte[] requestBytes = exchange.getRequestBody().readAllBytes();
            String body = new String(requestBytes, StandardCharsets.UTF_8);
            requestBody.set(body);
            String responseBody = """
                    {"envelope":%s,"state":"LOCAL_ACTION_ALLOWED","success":true,"message":"verified","controlCommandId":"cmd-hytale","controlCommandState":"COMPLETED","metadata":{"verifiedCategory":"ui"}}
                    """.formatted(body);
            byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
            exchange.close();
        });
        server.start();
        try {
            HytaleHttpControlCommandClient client = new HytaleHttpControlCommandClient(
                    java.net.URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/api/frontend/commands")
            );

            FrontendCommandEnvelope envelope = new HytaleFrontendCommandEnvelopeFactory().commandEnvelope(
                    "hytale-player",
                    "Builder",
                    "/kd ui",
                    "corr-http-hytale",
                    Map.of("server", "hytale-dev")
            );
            FrontendCommandVerificationResult result = client.submitCommand(envelope);

            assertTrue(result.success());
            assertEquals(FrontendCommandVerificationState.LOCAL_ACTION_ALLOWED, result.state());
            assertTrue(requestBody.get().contains("\"platform\":\"HYTALE\""));
            assertTrue(requestBody.get().contains("\"rawInput\":\"/kd ui\""));
        } finally {
            server.stop(0);
        }
    }
}
