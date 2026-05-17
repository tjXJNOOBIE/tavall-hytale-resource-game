package com.tavall.resourcegame.frontend.minecraft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.services.FrontendControlConfig;
import com.tavall.resourcegame.services.FrontendTcpControlBridgeResponse;
import com.tavall.resourcegame.services.FrontendTcpControlCommandClient;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlConfig;
import com.tavall.resourcegame.dependency.interfaces.IFrontendControlCommandClient;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.resourcegame.shared.frontend.RankOperationType;
import com.tavall.resourcegame.shared.frontend.PunishOperationType;
import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.PunishResponse;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendRuntime;
import com.tavall.resourcegame.shared.frontend.RankRequest;
import com.tavall.resourcegame.shared.frontend.RankResponse;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionRole;
import com.tjxjnoobie.api.dependency.DependencyLoader;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class MinecraftFrontendModuleTest {
    @AfterEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
        com.tavall.resourcegame.dependency.DependencyLoaderAccess.clear();
    }

    @Test
    void minecraftFrontendIsAThinPlatformAdapter() {
        MinecraftFrontendModule module = new MinecraftFrontendModule();

        assertEquals("minecraft-proxy", module.moduleName());
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
        registerMinecraftDependencies(null);
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
        registerMinecraftDependencies(submittedEnvelope);
        MinecraftControlPlaneCommandBridge bridge = new MinecraftControlPlaneCommandBridge();

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
    void minecraftControlPlaneBridgeHandlesOfflineControlIngressGracefully() throws IOException {
        int offlinePort = availablePort();
        com.tavall.resourcegame.dependency.DependencyLoaderAccess.registerInstance(
                IFrontendControlConfig.class,
                new FrontendControlConfig(URI.create("tcp://127.0.0.1:" + offlinePort), "velocity-test")
        );
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        MinecraftControlPlaneCommandBridge bridge = new MinecraftControlPlaneCommandBridge();

        FrontendCommandVerificationResult result = bridge.submitKdCommand(
                "minecraft-player",
                "Miner",
                List.of("kd", "resource", "status"),
                "corr-offline",
                Map.of("server", "kingdoms")
        );

        assertFalse(result.success());
        assertEquals(FrontendCommandVerificationState.REJECTED, result.state());
        assertTrue(result.message().startsWith("Control bridge unavailable:"));
    }

    @Test
    void minecraftFrontendDependencyModuleSelectsTcpClientForTcpUris() {
        com.tavall.resourcegame.dependency.DependencyLoaderAccess.registerInstance(
                IFrontendControlConfig.class,
                new FrontendControlConfig(URI.create("tcp://127.0.0.1:18081"), "velocity-test")
        );
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());

        IFrontendControlCommandClient client = com.tavall.resourcegame.dependency.DependencyLoaderAccess.findInstance(IFrontendControlCommandClient.class);
        assertTrue(client instanceof FrontendTcpControlCommandClient);
    }

    @Test
    void minecraftControlPlaneBridgeSerializesAndDeserializesThroughLocalBridge() throws Exception {
        try (ControlIngressFixture fixture = startControlIngressServer(envelope ->
                FrontendTcpControlBridgeResponse.success(dispatched(envelope, "dispatched-over-tcp")))) {
            com.tavall.resourcegame.dependency.DependencyLoaderAccess.registerInstance(
                    IFrontendControlConfig.class,
                    new FrontendControlConfig(fixture.ingressUri(), "velocity-test")
            );
            new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
            MinecraftControlPlaneCommandBridge bridge = new MinecraftControlPlaneCommandBridge();

            FrontendCommandVerificationResult result = bridge.submitKdCommand(
                    "minecraft-player",
                    "Miner",
                    List.of("kd", "resource", "status"),
                    "corr-http",
                    Map.of("server", "kingdoms")
            );

            assertTrue(result.success());
            assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
            assertEquals("dispatched-over-tcp", result.message());
            assertEquals("/kd resource status", fixture.submittedEnvelope().get().rawInput());
            assertTrue(fixture.rawRequestBody().contains("\"platform\":\"MINECRAFT\""));
            assertTrue(fixture.rawRequestBody().contains("\"rawInput\":\"/kd resource status\""));
        }
    }

    @Test
    void velocityCommandExecutionRequiresAdminPermissionForMutatingCommands() {
        registerMinecraftDependencies(null);
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler();

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
        registerMinecraftDependencies(submittedEnvelope);
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler();

        MinecraftVelocityCommandResult result = executionHandler.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "kd",
                new String[]{"clock", "override", "kingdom-1", "22:00"}
        );

        assertEquals("COMPLETED: dispatched", result.message());
        assertEquals("/kd clock override kingdom-1 22:00", submittedEnvelope.get().rawInput());
        assertEquals("velocity-test", submittedEnvelope.get().sourceMetadata().get("proxy"));
        assertEquals("VELOCITY_PROXY", submittedEnvelope.get().sourceMetadata().get("surfaceIdentity"));
        assertEquals("bukkit-server-snapshot-ingress", submittedEnvelope.get().sourceMetadata().get("serverDataSource"));
        assertEquals("test-player", submittedEnvelope.get().platformAccountId());
    }

    @Test
    void velocityRankCommandSubmitsListAndSetRequestsThroughControlPlane() {
        AtomicReference<RankRequest> submittedRankRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> {
                    submittedRankRequest.set(request);
                    if (request.operation() == com.tavall.resourcegame.shared.frontend.RankOperationType.LIST) {
                        return RankResponse.listed(
                                request.requestId(),
                                "Loaded 1 rank subject.",
                                List.of(),
                                Map.of("operation", request.operation().name())
                        );
                    }
                    return RankResponse.updated(
                            request.requestId(),
                            "Updated " + request.targetDisplayName() + " to " + request.requestedRole().name() + ".",
                            new com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject(
                                    ResourceGameFrontendPlatform.MINECRAFT,
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    request.requestedRole(),
                                    java.util.Set.of()
                            ),
                            List.of(),
                            Map.of("operation", request.operation().name())
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(
                new MinecraftProxyConfig(
                        "tavall.resourcegame.command",
                        "tavall.resourcegame.admin",
                        "velocity-test",
                        Map.of(),
                        Set.of("test player"),
                        Set.of(),
                        true
                ),
                MinecraftVelocityInstanceSwitchGateway.noop()
        );
        MinecraftVelocityRankCommand rankCommand = new MinecraftVelocityRankCommand();

        MinecraftVelocityCommandResult listResult = rankCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "rank",
                new String[]{"list"}
        );
        assertTrue(listResult.success());
        assertEquals(RankOperationType.LIST, submittedRankRequest.get().operation());
        assertTrue(listResult.message().contains("Loaded 1 rank subject."));

        MinecraftVelocityCommandResult setResult = rankCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "rank",
                new String[]{"set", "miner-1", "moderator"}
        );
        assertTrue(setResult.success());
        assertEquals(RankOperationType.SET_ROLE, submittedRankRequest.get().operation());
        assertEquals(UniversalPermissionRole.MODERATOR, submittedRankRequest.get().requestedRole());
        assertTrue(setResult.message().contains("Updated miner-1 to MODERATOR."));
    }

    @Test
    void velocityRankBaseCommandListsSubjectsWithoutAdminPermission() {
        AtomicReference<RankRequest> submittedRankRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> {
                    submittedRankRequest.set(request);
                    return RankResponse.listed(
                            request.requestId(),
                            "Loaded 2 rank subjects.",
                            List.of(),
                            Map.of("operation", request.operation().name())
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(
                new MinecraftProxyConfig(
                        "tavall.resourcegame.command",
                        "tavall.resourcegame.admin",
                        "velocity-test",
                        Map.of(),
                        Set.of("test player"),
                        Set.of(),
                        true
                ),
                MinecraftVelocityInstanceSwitchGateway.noop()
        );
        MinecraftVelocityRankCommand rankCommand = new MinecraftVelocityRankCommand();

        MinecraftVelocityCommandResult result = rankCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command")),
                "rank",
                new String[0]
        );

        assertTrue(result.success());
        assertEquals(RankOperationType.LIST, submittedRankRequest.get().operation());
        assertTrue(result.message().contains("Loaded 2 rank subjects."));
    }

    @Test
    void velocityRankCommandSubmitsSetRequestsWithoutLocalPermissionGate() {
        AtomicReference<RankRequest> submittedRankRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> {
                    submittedRankRequest.set(request);
                    return RankResponse.updated(
                            request.requestId(),
                            "Updated " + request.targetDisplayName() + " to " + request.requestedRole().name() + ".",
                            new com.tavall.resourcegame.shared.permissions.UniversalPermissionSubject(
                                    ResourceGameFrontendPlatform.MINECRAFT,
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    request.requestedRole(),
                                    Set.of()
                            ),
                            List.of(),
                            Map.of("operation", request.operation().name())
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        MinecraftVelocityRankCommand rankCommand = new MinecraftVelocityRankCommand();

        MinecraftVelocityCommandResult result = rankCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command")),
                "rank",
                new String[]{"set", "miner-1", "ADMIN"}
        );

        assertTrue(result.success());
        assertEquals(RankOperationType.SET_ROLE, submittedRankRequest.get().operation());
        assertEquals(UniversalPermissionRole.ADMIN, submittedRankRequest.get().requestedRole());
        assertTrue(result.message().contains("Updated miner-1 to ADMIN."));
    }

    @Test
    void velocityBanCommandSubmitsPunishRequestsThroughControlPlane() {
        AtomicReference<PunishRequest> submittedPunishRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."),
                request -> {
                    submittedPunishRequest.set(request);
                    return PunishResponse.updated(
                            request.requestId(),
                            "You have banned " + request.targetDisplayName() + " for " + request.durationText() + ".",
                            new com.tavall.resourcegame.shared.frontend.PunishRecord(
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    PunishOperationType.BAN,
                                    true,
                                    request.reason(),
                                    request.actorDisplayName(),
                                    request.durationText(),
                                    request.createdAtEpochMillis(),
                                    null,
                                    Map.of("operation", request.operation().name())
                            ),
                            List.of(),
                            Map.of("operation", request.operation().name())
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        Ban banCommand = new Ban();

        MinecraftVelocityCommandResult result = banCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "ban",
                new String[]{"miner-1", "1h", "Testing", "ban", "flow"}
        );

        assertTrue(result.success());
        assertEquals(PunishOperationType.BAN, submittedPunishRequest.get().operation());
        assertEquals("miner-1", submittedPunishRequest.get().targetDisplayName());
        assertEquals("Testing ban flow", submittedPunishRequest.get().reason());
        assertTrue(result.message().contains("You have banned miner-1"));
    }

    @Test
    void velocityBanCommandRejectsInvalidDurationWithoutSubmittingToControlPlane() {
        AtomicReference<PunishRequest> submittedPunishRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."),
                request -> {
                    submittedPunishRequest.set(request);
                    return PunishResponse.updated(
                            request.requestId(),
                            "unexpected",
                            new com.tavall.resourcegame.shared.frontend.PunishRecord(
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    PunishOperationType.BAN,
                                    true,
                                    request.reason(),
                                    request.actorDisplayName(),
                                    request.durationText(),
                                    request.createdAtEpochMillis(),
                                    null,
                                    Map.of()
                            ),
                            List.of(),
                            Map.of()
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        Ban banCommand = new Ban();

        MinecraftVelocityCommandResult result = banCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "ban",
                new String[]{"miner-1", "not-a-duration", "Testing"}
        );

        assertFalse(result.success());
        assertTrue(result.message().contains("Invalid ban duration format"));
        assertNull(submittedPunishRequest.get());
    }

    @Test
    void velocityWarnCommandSupportsMultiWordReasonText() {
        AtomicReference<PunishRequest> submittedPunishRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."),
                request -> {
                    submittedPunishRequest.set(request);
                    return PunishResponse.updated(
                            request.requestId(),
                            "You have warned " + request.targetDisplayName() + ". Total warns: 1",
                            new com.tavall.resourcegame.shared.frontend.PunishRecord(
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    PunishOperationType.WARN,
                                    false,
                                    request.reason(),
                                    request.actorDisplayName(),
                                    "",
                                    request.createdAtEpochMillis(),
                                    null,
                                    Map.of()
                            ),
                            List.of(),
                            Map.of()
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        Warn warnCommand = new Warn();

        MinecraftVelocityCommandResult result = warnCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "warn",
                new String[]{"miner-1", "Repeated", "chat", "spam"}
        );

        assertTrue(result.success());
        assertEquals("Repeated chat spam", submittedPunishRequest.get().reason());
        assertTrue(result.message().contains("You have warned miner-1"));
    }

    @Test
    void velocityWarnCommandSubmitsPunishRequestsThroughControlPlane() {
        AtomicReference<PunishRequest> submittedPunishRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."),
                request -> {
                    submittedPunishRequest.set(request);
                    return PunishResponse.updated(
                            request.requestId(),
                            "You have warned " + request.targetDisplayName() + ". Total warns: 1",
                            new com.tavall.resourcegame.shared.frontend.PunishRecord(
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    PunishOperationType.WARN,
                                    false,
                                    request.reason(),
                                    request.actorDisplayName(),
                                    "",
                                    request.createdAtEpochMillis(),
                                    null,
                                    Map.of("operation", request.operation().name())
                            ),
                            List.of(),
                            Map.of("operation", request.operation().name())
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        Warn warnCommand = new Warn();

        MinecraftVelocityCommandResult result = warnCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "warn",
                new String[]{"miner-1", "Testing"}
        );

        assertTrue(result.success());
        assertEquals(PunishOperationType.WARN, submittedPunishRequest.get().operation());
        assertEquals("miner-1", submittedPunishRequest.get().targetDisplayName());
        assertTrue(result.message().contains("You have warned miner-1"));
    }

    @Test
    void velocityUnbanCommandSubmitsPunishRequestsThroughControlPlane() {
        AtomicReference<PunishRequest> submittedPunishRequest = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."),
                request -> {
                    submittedPunishRequest.set(request);
                    return PunishResponse.updated(
                            request.requestId(),
                            "You have unbanned " + request.targetDisplayName() + ".",
                            new com.tavall.resourcegame.shared.frontend.PunishRecord(
                                    request.targetPlatformAccountId(),
                                    request.targetDisplayName(),
                                    PunishOperationType.UNBAN,
                                    false,
                                    "Unbanned",
                                    request.actorDisplayName(),
                                    "Permanent",
                                    request.createdAtEpochMillis(),
                                    null,
                                    Map.of("operation", request.operation().name())
                            ),
                            List.of(),
                            Map.of("operation", request.operation().name())
                    );
                }
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        Unban unbanCommand = new Unban();

        MinecraftVelocityCommandResult result = unbanCommand.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "unban",
                new String[]{"miner-1"}
        );

        assertTrue(result.success());
        assertEquals(PunishOperationType.UNBAN, submittedPunishRequest.get().operation());
        assertEquals("miner-1", submittedPunishRequest.get().targetDisplayName());
        assertTrue(result.message().contains("You have unbanned miner-1"));
    }

    @Test
    void velocityLoginEventDisconnectsBannedPlayer() {
        AtomicBoolean disconnected = new AtomicBoolean(false);
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(
                envelope -> dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput())),
                request -> RankResponse.unavailable(request.requestId(), "Rank request not mocked."),
                request -> PunishResponse.updated(
                        request.requestId(),
                        "Loaded active punishment for ResourceProxyBot.",
                        new com.tavall.resourcegame.shared.frontend.PunishRecord(
                                request.targetPlatformAccountId(),
                                request.targetDisplayName(),
                                PunishOperationType.BAN,
                                true,
                                "Testing",
                                "Console",
                                "Permanent",
                                request.createdAtEpochMillis(),
                                null,
                                Map.of("operation", request.operation().name())
                        ),
                        List.of(),
                        Map.of("operation", request.operation().name())
                )
        ));
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
        MinecraftVelocityLoginEvent loginEvent = new MinecraftVelocityLoginEvent();

        loginEvent.handleLogin(testVelocityPlayer("ResourceProxyBot", disconnected));

        assertTrue(disconnected.get());
    }

    @Test
    void velocityInstanceSwitchExecutesProxyTransferAndConfirmsThroughControlPlane() {
        AtomicReference<FrontendCommandEnvelope> completionEnvelope = new AtomicReference<>();
        AtomicReference<String> targetInstance = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(envelope -> {
            if (envelope.rawInput().startsWith("/kd instance confirm")) {
                completionEnvelope.set(envelope);
                return dispatched(envelope, "confirmed", "cmd-confirm", Map.of("controlConsoleInput", envelope.rawInput()));
            }
            return dispatched(
                    envelope,
                    "requested",
                    "cmd-switch",
                    Map.of(
                            "controlConsoleInput", envelope.rawInput(),
                            "commandType", "REQUEST_INSTANCE_SWITCH",
                            "instanceSwitchRequestId", "switch-1",
                            "changedObjectIds", "instance-switch:switch-1,platform-instance:kingdom-2-minecraft-primary"
                    )
            );
        }));
        MinecraftVelocityInstanceSwitchGateway gateway = (platformAccountId, targetInstanceId) -> {
            targetInstance.set(targetInstanceId);
            return java.util.concurrent.CompletableFuture.completedFuture(MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult.success("ffa"));
        };
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), gateway);
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler();

        MinecraftVelocityCommandResult result = executionHandler.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "kd",
                new String[]{"instance", "switch", "player-1", "minecraft", "kingdom-1", "kingdom-2"}
        );

        assertTrue(result.success());
        assertEquals("kingdom-2-minecraft-primary", targetInstance.get());
        assertEquals("/kd instance confirm switch-1", completionEnvelope.get().rawInput());
        assertTrue(result.message().contains("Velocity switch connected to ffa."));
    }

    @Test
    void velocityInstanceSwitchFailureReportsBackThroughControlPlane() {
        AtomicReference<FrontendCommandEnvelope> failureEnvelope = new AtomicReference<>();
        DependencyLoaderAccess.registerInstance(IMinecraftControlCommandClient.class, new RecordingMinecraftControlCommandClient(envelope -> {
            if (envelope.rawInput().startsWith("/kd instance fail")) {
                failureEnvelope.set(envelope);
                return dispatched(envelope, "failed-recorded", "cmd-fail", Map.of("controlConsoleInput", envelope.rawInput()));
            }
            return dispatched(
                    envelope,
                    "requested",
                    "cmd-switch",
                    Map.of(
                            "controlConsoleInput", envelope.rawInput(),
                            "commandType", "REQUEST_INSTANCE_SWITCH",
                            "instanceSwitchRequestId", "switch-2",
                            "changedObjectIds", "instance-switch:switch-2,platform-instance:missing-instance"
                    )
            );
        }));
        MinecraftVelocityInstanceSwitchGateway gateway = (platformAccountId, targetInstanceId) -> java.util.concurrent.CompletableFuture.completedFuture(
                MinecraftVelocityInstanceSwitchGateway.MinecraftVelocityInstanceSwitchResult.failed(targetInstanceId, "Velocity server is not registered: missing-instance.")
        );
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), gateway);
        MinecraftVelocityCommandExecutionHandler executionHandler = new MinecraftVelocityCommandExecutionHandler();

        MinecraftVelocityCommandResult result = executionHandler.execute(
                new TestVelocityCommandSource(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin")),
                "kd",
                new String[]{"instance", "switch", "player-1", "minecraft", "kingdom-1", "kingdom-2"}
        );

        assertFalse(result.success());
        assertEquals("/kd instance fail switch-2 Velocity_server_is_not_registered_missing-instance.", failureEnvelope.get().rawInput());
    }

    @Test
    void velocityPermissionResolverAllowsConfiguredOwnerUsernameForAdminCommands() {
        new MinecraftFrontendDependencyModule().registerDependencies(
                new MinecraftProxyConfig(
                        "tavall.resourcegame.command",
                        "tavall.resourcegame.admin",
                        "velocity-test",
                        Map.of(),
                        Set.of("test player"),
                        Set.of(),
                        true
                ),
                MinecraftVelocityInstanceSwitchGateway.noop()
        );
        MinecraftVelocityCommandPermissionHandler permissionHandler = new MinecraftVelocityCommandPermissionHandler();

        assertTrue(permissionHandler.canExecute(new TestVelocityCommandSource(Set.of()), "kd", new String[]{"clock", "override", "kingdom-1", "22:00"}));
    }

    @Test
    void velocityPluginRegistersProxyRuntimeBehindMinecraftInterface() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/tavall/resourcegame/frontend/minecraft/MinecraftVelocityProxyPlugin.java"));

        assertTrue(source.contains("IMinecraftVelocityProxyServer.class"));
        assertTrue(source.contains("new MinecraftVelocityProxyServerAdapter(proxyServer)"));
        assertFalse(source.contains("metaBuilder(\"kd\")"));
        assertFalse(source.contains(".aliases(\"kingdom\")"));
        assertFalse(source.contains("registerInstance(ProxyServer.class"));
    }

    private void registerMinecraftDependencies(AtomicReference<FrontendCommandEnvelope> submittedEnvelope) {
        DependencyLoaderAccess.registerInstance(
                IMinecraftControlCommandClient.class,
                new RecordingMinecraftControlCommandClient(envelope -> {
                    if (submittedEnvelope != null) {
                        submittedEnvelope.set(envelope);
                    }
                    return dispatched(envelope, "dispatched", "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput()));
                })
        );
        new MinecraftFrontendDependencyModule().registerDependencies(defaultConfig(), MinecraftVelocityInstanceSwitchGateway.noop());
    }

    private MinecraftProxyConfig defaultConfig() {
        return new MinecraftProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-test",
                Map.of(),
                Set.of(),
                Set.of(),
                true
        );
    }

    private FrontendCommandVerificationResult dispatched(
            FrontendCommandEnvelope envelope,
            String message,
            String commandId,
            Map<String, String> metadata
    ) {
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.DISPATCHED,
                true,
                message,
                commandId,
                "COMPLETED",
                metadata
        );
    }

    private FrontendCommandVerificationResult dispatched(FrontendCommandEnvelope envelope, String message) {
        return dispatched(envelope, message, "cmd-minecraft", Map.of("controlConsoleInput", envelope.rawInput()));
    }

    private com.velocitypowered.api.proxy.Player testVelocityPlayer(String username, AtomicBoolean disconnected) {
        InvocationHandler handler = (proxy, method, args) -> {
            String methodName = method.getName();
            if (methodName.equals("getUniqueId")) {
                return java.util.UUID.nameUUIDFromBytes(username.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
            if (methodName.equals("getUsername")) {
                return username;
            }
            if (methodName.equals("disconnect")) {
                disconnected.set(true);
                return null;
            }
            if (methodName.equals("hasPermission")) {
                return true;
            }
            if (method.getReturnType().equals(boolean.class)) {
                return false;
            }
            if (method.getReturnType().equals(int.class)) {
                return 0;
            }
            if (method.getReturnType().equals(long.class)) {
                return 0L;
            }
            if (method.getReturnType().equals(double.class)) {
                return 0D;
            }
            return null;
        };
        return (com.velocitypowered.api.proxy.Player) Proxy.newProxyInstance(
                com.velocitypowered.api.proxy.Player.class.getClassLoader(),
                new Class<?>[]{com.velocitypowered.api.proxy.Player.class},
                handler
        );
    }

    private int availablePort() throws IOException {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    private ControlIngressFixture startControlIngressServer(Function<FrontendCommandEnvelope, FrontendTcpControlBridgeResponse> responder) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("127.0.0.1", 0));
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        AtomicReference<String> rawRequestBody = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Thread acceptThread = new Thread(() -> handleControlIngress(serverSocket, objectMapper, submittedEnvelope, rawRequestBody, failure, responder),
                "minecraft-control-ingress-test");
        acceptThread.setDaemon(true);
        acceptThread.start();
        return new ControlIngressFixture(
                serverSocket,
                URI.create("tcp://127.0.0.1:" + serverSocket.getLocalPort()),
                submittedEnvelope,
                rawRequestBody,
                failure,
                acceptThread
        );
    }

    private void handleControlIngress(
            ServerSocket serverSocket,
            ObjectMapper objectMapper,
            AtomicReference<FrontendCommandEnvelope> submittedEnvelope,
            AtomicReference<String> rawRequestBody,
            AtomicReference<Throwable> failure,
            Function<FrontendCommandEnvelope, FrontendTcpControlBridgeResponse> responder
    ) {
        try (
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            String requestBody = reader.readLine();
            rawRequestBody.set(requestBody);
            FrontendCommandEnvelope envelope = objectMapper.readValue(requestBody, FrontendCommandEnvelope.class);
            submittedEnvelope.set(envelope);

            FrontendTcpControlBridgeResponse response = responder.apply(envelope);
            writer.write(objectMapper.writeValueAsString(response));
            writer.newLine();
            writer.flush();
        } catch (Throwable exception) {
            if (!serverSocket.isClosed()) {
                failure.set(exception);
            }
        }
    }

    private static final class ControlIngressFixture implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final URI ingressUri;
        private final AtomicReference<FrontendCommandEnvelope> submittedEnvelope;
        private final AtomicReference<String> rawRequestBody;
        private final AtomicReference<Throwable> failure;
        private final Thread acceptThread;

        private ControlIngressFixture(
                ServerSocket serverSocket,
                URI ingressUri,
                AtomicReference<FrontendCommandEnvelope> submittedEnvelope,
                AtomicReference<String> rawRequestBody,
                AtomicReference<Throwable> failure,
                Thread acceptThread
        ) {
            this.serverSocket = serverSocket;
            this.ingressUri = ingressUri;
            this.submittedEnvelope = submittedEnvelope;
            this.rawRequestBody = rawRequestBody;
            this.failure = failure;
            this.acceptThread = acceptThread;
        }

        private URI ingressUri() {
            return ingressUri;
        }

        private AtomicReference<FrontendCommandEnvelope> submittedEnvelope() {
            return submittedEnvelope;
        }

        private String rawRequestBody() {
            return rawRequestBody.get();
        }

        @Override
        public void close() throws Exception {
            serverSocket.close();
            acceptThread.join(1000L);
            if (failure.get() != null) {
                throw new IllegalStateException("Control ingress fixture failed.", failure.get());
            }
        }
    }
}
