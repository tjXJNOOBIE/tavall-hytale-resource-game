package com.tavall.hytale.resourcegame.frontend.minecraft;

import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.hytale.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.hytale.resourcegame.shared.frontend.FrontendCommandVerificationState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MinecraftFrontendModuleTest {
    @Test
    void minecraftFrontendIsAThinPlatformAdapter() {
        MinecraftFrontendModule module = new MinecraftFrontendModule();

        assertEquals("tavall-resource-game-minecraft-frontend", module.moduleName());
        assertEquals("MINECRAFT", module.platformKey());
        assertEquals(ResourceGameFrontendPlatform.MINECRAFT, module.descriptor().platform());
        assertEquals(ResourceGameFrontendRuntime.MINECRAFT_VELOCITY_PROXY_PLUGIN, module.descriptor().runtime());
        assertEquals("FrontendCommandIngressHandler", module.commandPipelineEntryPoint());
        assertFalse(module.ownsCanonicalGameplayState());
    }

    @Test
    void minecraftCommandEnvelopeTargetsControlIngress() {
        MinecraftFrontendCommandEnvelopeFactory factory = new MinecraftFrontendCommandEnvelopeFactory();

        FrontendCommandEnvelope envelope = factory.commandEnvelope(
                "minecraft-player",
                "Miner",
                "/kd resources",
                "corr-minecraft",
                Map.of("server", "kingdoms")
        );

        assertEquals(ResourceGameFrontendPlatform.MINECRAFT, envelope.platform());
        assertEquals("/kd resources", envelope.rawInput());
    }

    @Test
    void minecraftKdBridgeCreatesNativeCommandEnvelope() {
        MinecraftKdCommandEnvelopeBridge bridge = new MinecraftKdCommandEnvelopeBridge();

        FrontendCommandEnvelope envelope = bridge.commandEnvelope(
                "minecraft-player",
                "Miner",
                java.util.List.of("kd", "troops", "debug", "troop-1"),
                "corr-minecraft-kd",
                Map.of("server", "kingdoms")
        );

        assertEquals(ResourceGameFrontendPlatform.MINECRAFT, envelope.platform());
        assertEquals("/kd troops debug troop-1", envelope.rawInput());
        assertEquals("kingdoms", envelope.sourceMetadata().get("server"));
    }

    @Test
    void minecraftControlPlaneBridgeSubmitsKdCommandToClient() {
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        MinecraftControlPlaneCommandBridge bridge = new MinecraftControlPlaneCommandBridge(
                new MinecraftKdCommandEnvelopeBridge(),
                envelope -> {
                    submittedEnvelope.set(envelope);
                    return new FrontendCommandVerificationResult(
                            envelope,
                            FrontendCommandVerificationState.DISPATCHED,
                            true,
                            "dispatched",
                            "cmd-minecraft",
                            "COMPLETED",
                            Map.of("controlConsoleInput", "troop debug troop-1")
                    );
                }
        );

        FrontendCommandVerificationResult result = bridge.submitKdCommand(
                "minecraft-player",
                "Miner",
                List.of("kd", "troops", "debug", "troop-1"),
                "corr-minecraft-submit",
                Map.of("server", "kingdoms")
        );

        assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
        assertEquals("/kd troops debug troop-1", submittedEnvelope.get().rawInput());
        assertEquals(ResourceGameFrontendPlatform.MINECRAFT, submittedEnvelope.get().platform());
    }

    @Test
    void velocityCommandExecutionRequiresAdminPermissionForMutatingCommands() {
        MinecraftVelocityCommandPermissionHandler permissionHandler = new MinecraftVelocityCommandPermissionHandler(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin"
        );
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler(
                bridgeThatRecords(null),
                permissionHandler,
                "velocity-test"
        );

        MinecraftVelocityCommandResult denied = executionHandler.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command")),
                "kd",
                new String[]{"citizens", "spawn", "player-1", "1", "kingdom-1"}
        );

        assertFalse(denied.success());
        assertEquals("Missing permission tavall.resourcegame.admin.", denied.message());

        MinecraftVelocityCommandResult kingdomCreateDenied = executionHandler.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command")),
                "kd",
                new String[]{"kingdom", "create", "--displayName", "First", "--worldId", "default", "--borderSize", "1000"}
        );

        assertFalse(kingdomCreateDenied.success());
        assertEquals("Missing permission tavall.resourcegame.admin.", kingdomCreateDenied.message());
    }

    @Test
    void velocityCommandExecutionSubmitsPermittedGameCommandToControlPlane() {
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler(
                bridgeThatRecords(submittedEnvelope),
                new MinecraftVelocityCommandPermissionHandler("tavall.resourcegame.command", "tavall.resourcegame.admin"),
                "velocity-test"
        );

        MinecraftVelocityCommandResult result = executionHandler.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "kd",
                new String[]{"clock", "override", "kingdom-1", "22:00"}
        );

        assertEquals("COMPLETED: dispatched", result.message());
        assertEquals("/kd clock override kingdom-1 22:00", submittedEnvelope.get().rawInput());
        assertEquals("velocity-test", submittedEnvelope.get().sourceMetadata().get("proxy"));
        assertEquals("test-player", submittedEnvelope.get().platformAccountId());
    }

    @Test
    void velocityPermissionResolverAllowsConfiguredOwnerUsernameForAdminCommands() {
        MinecraftVelocityCommandPermissionHandler permissionHandler = new MinecraftVelocityCommandPermissionHandler(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                new MinecraftVelocityPermissionResolver(
                        "tavall.resourcegame.command",
                        "tavall.resourcegame.admin",
                        Set.of("test player"),
                        Set.of(),
                        true
                ),
                new com.tavall.hytale.resourcegame.shared.permissions.UniversalPermissionPolicy()
        );

        assertTrue(permissionHandler.canExecute(new TestVelocityCommandSource(Set.of()), "kd", new String[]{"clock", "override", "kingdom-1", "22:00"}));
    }

    private MinecraftControlPlaneCommandBridge bridgeThatRecords(AtomicReference<FrontendCommandEnvelope> submittedEnvelope) {
        return new MinecraftControlPlaneCommandBridge(
                new MinecraftKdCommandEnvelopeBridge(),
                envelope -> {
                    if (submittedEnvelope != null) {
                        submittedEnvelope.set(envelope);
                    }
                    return new FrontendCommandVerificationResult(
                            envelope,
                            FrontendCommandVerificationState.DISPATCHED,
                            true,
                            "dispatched",
                            "cmd-minecraft",
                            "COMPLETED",
                            Map.of("controlConsoleInput", envelope.rawInput())
                    );
                }
        );
    }
}
