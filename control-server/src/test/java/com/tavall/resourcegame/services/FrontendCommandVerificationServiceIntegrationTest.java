package org.tavall.control.runtime;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.api.minecraft.frontend.IFrontendControlCommandClient;
import org.tavall.api.minecraft.frontend.IFrontendControlConfig;
import org.tavall.control.transport.FrontendControlConfig;
import org.tavall.control.runtime.ControlCommandRuntime;
import org.tavall.control.runtime.ControlCommandRuntimeFactory;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontendCommandVerificationServiceIntegrationTest {
    @AfterEach
    void clearExternalDependencies() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void hytaleKdCommandIsDispatchedThroughControlIngress() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        registerFrontendControlDependencies(runtime);
        FrontendCommandVerificationHandler handler = new FrontendCommandVerificationHandler();

        FrontendCommandVerificationResult result = handler.verifyHytaleKdCommand(
                "hytale-player-1",
                "Builder",
                List.of("ui", "castle-main"),
                Map.of("server", "hytale-dev")
        );

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertTrue(result.success());
        assertEquals("frontend kd ui kd ui castle-main", result.metadata().get("controlConsoleInput"));
        assertFalse(runtime.auditLogRepository().findRecentAuditLogs(10).isEmpty());
    }

    @Test
    void translatedKdCommandDispatchesAndDoesNotNeedAdapterMutation() {
        ControlCommandRuntime runtime = ControlCommandRuntimeFactory.createInMemoryRuntime();
        registerFrontendControlDependencies(runtime);
        FrontendCommandVerificationHandler handler = new FrontendCommandVerificationHandler();

        FrontendCommandVerificationResult result = handler.verifyHytaleKdCommand(
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

    @Test
    void frontendControlConfigAcceptsRemoteHytaleEnvironmentNames() {
        FrontendControlConfig config = FrontendControlConfig.fromEnvironment(Map.of(
                "RESOURCE_GAME_HYTALE_CONTROL_INGRESS_URL", "tcp://127.0.0.1:19081",
                "RESOURCE_GAME_HYTALE_SERVER_ID", "remote-hytale"
        ));

        assertEquals(URI.create("tcp://127.0.0.1:19081"), config.controlIngressUri());
        assertEquals("remote-hytale", config.serverId());
    }

    @Test
    void frontendControlConfigRejectsHttpControlIngress() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                FrontendControlConfig.fromEnvironment(Map.of(
                        "RESOURCE_GAME_CONTROL_INGRESS_URL", "http://127.0.0.1:18080"
                ))
        );

        assertTrue(exception.getMessage().contains("tcp://host:port"));
    }

    private void registerFrontendControlDependencies(ControlCommandRuntime runtime) {
        DependencyLoaderAccess.registerInstance(
                IFrontendControlConfig.class,
                new FrontendControlConfig(URI.create("tcp://127.0.0.1:18081"), "hytale-single-server")
        );
        DependencyLoaderAccess.registerInstance(IFrontendControlCommandClient.class, new RuntimeBackedFrontendControlCommandClient(runtime));
    }
}
