package org.tavall.minecraft.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.tavall.api.minecraft.frontend.FrontendCommandEnvelope;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationResult;
import org.tavall.api.minecraft.frontend.FrontendCommandVerificationState;
import org.tavall.api.minecraft.frontend.FrontendControlConfig;
import org.tavall.api.minecraft.frontend.transport.ControlPlaneTcpBridgeRequest;
import org.tavall.api.minecraft.frontend.transport.FrontendTcpControlBridgeResponse;
import org.tavall.api.minecraft.frontend.transport.JsonMapperProvider;
import org.tavall.api.minecraft.interaction.InteractionMenuElement;
import org.tavall.api.minecraft.interaction.InteractionMenuModel;
import org.tavall.api.minecraft.interaction.InteractionRequest;
import org.tavall.api.minecraft.interaction.InteractionResult;
import org.tavall.api.minecraft.interaction.InteractionResultType;
import org.tavall.api.minecraft.interaction.InteractionTargetType;
import org.tavall.api.minecraft.MinecraftServerRuntimeSnapshot;
import org.tavall.api.minecraft.MinecraftVisualRenderRequest;
import org.tavall.api.minecraft.player.PlayerDataRequest;
import org.tavall.api.minecraft.player.PlayerDataResponse;
import org.tavall.api.minecraft.player.PlayerPlatformBindingView;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendSurfaceIdentity;
import org.tavall.minecraft.server.json.IMinecraftBukkitJsonMapper;
import org.tavall.minecraft.server.json.MinecraftBukkitJsonHandler;
import org.tavall.minecraft.server.json.MinecraftBukkitJsonMapper;
import org.tavall.minecraft.server.snapshot.MinecraftBukkitSnapshotHandler;
import com.tjxjnoobie.api.dependency.DependencyLoader;
import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MinecraftBukkitServerFrontendTest {
    @Test
    void configUsesServerIdentityWithoutNetworkIngress() {
        Map<String, String> environment = new LinkedHashMap<String, String>();
        environment.put("RESOURCE_GAME_MINECRAFT_SERVER_ID", "ffa");
        environment.put("RESOURCE_GAME_MINECRAFT_PROXY_ID", "velocity-proxy");
        environment.put("RESOURCE_GAME_MINECRAFT_SNAPSHOT_INTERVAL_TICKS", "40");
        environment.put("RESOURCE_GAME_MINECRAFT_RESOURCE_PACK_PATH", "resource-pack/custom-assets/");
        MinecraftBukkitServerConfig config = MinecraftBukkitServerConfig.fromEnvironment(environment);

        assertEquals("ffa", config.serverId());
        assertEquals("velocity-proxy", config.proxyId());
        assertEquals(40L, config.snapshotIntervalTicks());
        assertEquals("resource-pack/custom-assets/", config.resourcePackPath());
    }

    @Test
    void configFallsBackToDefaultResourcePackPath() {
        Map<String, String> environment = new LinkedHashMap<String, String>();
        MinecraftBukkitServerConfig config = MinecraftBukkitServerConfig.fromEnvironment(environment);

        assertEquals("resource-pack/", config.resourcePackPath());
    }

    @Test
    void snapshotHandlerBuildsBukkitServerSurfacePayload() {
        registerBukkitFrontendDependencies(new MinecraftBukkitServerConfig(
                "ffa",
                "velocity-proxy",
                200L,
                "resource-pack/"
        ), 1000L, URI.create("tcp://127.0.0.1:18081"));

        MinecraftServerRuntimeSnapshot snapshot = new MinecraftBukkitSnapshotHandler().createSnapshot(new FakeMinecraftBukkitServerView(), 2000L);

        assertEquals(ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER, snapshot.surfaceIdentity());
        assertEquals("ffa", snapshot.serverId());
        assertEquals("velocity-proxy", snapshot.proxyId());
        assertEquals(1, snapshot.onlinePlayerCount());
        assertEquals("Miner", snapshot.players().getFirst().playerName());
        assertEquals("direct-control-ingress", snapshot.metadata().get("dataPath"));
    }

    @Test
    void jsonHandlerSerializesSnapshotShapeAcceptedByControlIngress() throws Exception {
        registerBukkitFrontendDependencies(new MinecraftBukkitServerConfig(
                "ffa",
                "velocity-proxy",
                200L,
                "resource-pack/"
        ), 1000L, URI.create("tcp://127.0.0.1:18081"));
        MinecraftServerRuntimeSnapshot snapshot = new MinecraftBukkitSnapshotHandler()
                .createSnapshot(new FakeMinecraftBukkitServerView(), 2000L);

        String json = new MinecraftBukkitJsonHandler().writeJson(snapshot);

        assertTrue(json.contains("\"surfaceIdentity\":\"BUKKIT_SERVER\""));
        assertTrue(json.contains("\"serverId\":\"ffa\""));
        assertTrue(json.contains("\"playerName\":\"Miner\""));
    }

    @Test
    void commandClientDispatchesInteractionThroughRemoteControlIngress() throws Exception {
        try (ControlIngressFixture fixture = startControlIngressServer(request -> {
            InteractionRequest interactionRequest = request.interactionRequest();
            if (interactionRequest == null) {
                return FrontendTcpControlBridgeResponse.error("Expected an interaction request.");
            }
            return FrontendTcpControlBridgeResponse.success(interactionDispatched(interactionRequest, "interaction-dispatched"));
        })) {
            registerBukkitFrontendDependencies(new MinecraftBukkitServerConfig(
                    "kingdom",
                    "velocity-proxy",
                    200L,
                    "resource-pack/"
            ), 1000L, fixture.ingressUri());

            InteractionResult result = new MinecraftBukkitCommandClientHandler().submitInteraction(
                    new InteractionRequest(
                            "corr-server-command-interact",
                            "player-1",
                            InteractionTargetType.UNKNOWN,
                            "player-1",
                            "open_menu",
                            "kingdom",
                            "world",
                            Map.of("message", "diagnostic"),
                            1L
                    )
            );

            assertTrue(result.success());
            assertEquals(InteractionResultType.OPEN_MENU, result.resultType());
            assertEquals("interaction-dispatched", result.message());
            assertTrue(result.menu() != null);
            assertTrue(fixture.rawRequestBody().contains("\"requestType\":\"INTERACTION_REQUEST\""));
        }
    }

    @Test
    void commandClientFetchesPlayerDataThroughRemoteControlIngress() throws Exception {
        try (ControlIngressFixture fixture = startControlIngressServer(request -> {
            PlayerDataRequest playerDataRequest = request.playerDataRequest();
            if (playerDataRequest == null) {
                return FrontendTcpControlBridgeResponse.error("Expected a player data request.");
            }
            return FrontendTcpControlBridgeResponse.success(playerData(playerDataRequest));
        })) {
            registerBukkitFrontendDependencies(new MinecraftBukkitServerConfig(
                    "kingdom",
                    "velocity-proxy",
                    200L,
                    "resource-pack/"
            ), 1000L, fixture.ingressUri());

            PlayerDataResponse result = new MinecraftBukkitCommandClientHandler().fetchPlayerData(
                    new PlayerDataRequest(
                            "corr-player-data",
                            UUID.fromString("00000000-0000-0000-0000-000000000123"),
                            "Miner",
                            "kingdom",
                            "world",
                            Map.of("surfaceIdentity", "BUKKIT_SERVER"),
                            2L
                    )
            );

            assertTrue(result.success());
            assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000123"), result.playerId());
            assertEquals("Miner", result.displayName());
            assertEquals("Player profile loaded.", result.message());
            assertTrue(result.accountExists());
            assertEquals(1, result.platformBindings().size());
            assertTrue(fixture.rawRequestBody().contains("\"requestType\":\"PLAYER_DATA_REQUEST\""));
        }
    }

    @Test
    void commandClientDispatchesServerKdCommandThroughRemoteControlIngress() throws Exception {
        try (ControlIngressFixture fixture = startControlIngressServer(request -> {
            FrontendCommandEnvelope envelope = request.frontendCommand();
            if (envelope == null) {
                return FrontendTcpControlBridgeResponse.error("Expected a command envelope.");
            }
            return FrontendTcpControlBridgeResponse.success(dispatched(envelope, "command-dispatched"));
        })) {
            registerBukkitFrontendDependencies(new MinecraftBukkitServerConfig(
                    "kingdom",
                    "velocity-proxy",
                    200L,
                    "resource-pack/"
            ), 1000L, fixture.ingressUri());

            var result = new MinecraftBukkitCommandClientHandler().submitCommand(
                    "minecraft-console:kingdom",
                    "CONSOLE",
                    "kd clock state kingdom-1",
                    "corr-server-kd",
                    Map.of("surfaceIdentity", "BUKKIT_SERVER")
            );

            assertTrue(result.success());
            assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
            assertEquals("clock state kingdom-1", result.metadata().get("controlConsoleInput"));
            assertEquals("kd clock state kingdom-1", fixture.submittedEnvelope().get().rawInput());
            assertTrue(fixture.rawRequestBody().contains("\"rawInput\":\"kd clock state kingdom-1\""));
        }
    }

    @Test
    void commandClientReturnsRejectedResultWhenControlIngressIsOffline() throws Exception {
        int offlinePort = findAvailablePort();
        URI offlineIngressUri = URI.create("tcp://127.0.0.1:" + offlinePort);

        registerBukkitFrontendDependencies(new MinecraftBukkitServerConfig(
                "kingdom",
                "velocity-proxy",
                200L,
                "resource-pack/"
        ), 1000L, offlineIngressUri);

        var result = new MinecraftBukkitCommandClientHandler().submitCommand(
                "minecraft-console:kingdom",
                "CONSOLE",
                "kd clock state kingdom-1",
                "corr-server-offline",
                Map.of("surfaceIdentity", "BUKKIT_SERVER")
        );

        assertFalse(result.success());
        assertEquals(FrontendCommandVerificationState.REJECTED, result.state());
        assertTrue(result.message().startsWith("Control bridge unavailable:"));
    }

    @Test
    void jsonHandlerSerializesVisualRenderRequestForServerSurface() throws IOException {
        DependencyLoader.getDependencyLoader().clear();
        com.tjxjnoobie.api.dependency.DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitJsonMapper.class, new MinecraftBukkitJsonMapper());
        MinecraftVisualRenderRequest request = new MinecraftVisualRenderRequest(
                ResourceGameFrontendSurfaceIdentity.BUKKIT_SERVER,
                "kingdom",
                "player-1",
                "TITLE",
                "Tavall Resource Game",
                "Server visual path is online.",
                Map.of("command", "tavallserver visual"),
                "corr-visual"
        );

        String json = new MinecraftBukkitJsonHandler().writeJson(request);

        assertTrue(json.contains("\"targetSurfaceIdentity\":\"BUKKIT_SERVER\""));
        assertTrue(json.contains("\"serverId\":\"kingdom\""));
        assertTrue(json.contains("\"visualType\":\"TITLE\""));
        assertTrue(json.contains("\"correlationId\":\"corr-visual\""));
    }

    private void registerBukkitFrontendDependencies(MinecraftBukkitServerConfig config, long startedAtEpochMillis, URI controlIngressUri) {
        DependencyLoader.getDependencyLoader().clear();
        com.tjxjnoobie.api.dependency.DependencyLoaderAccess.clear();
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitServerConfig.class, config);
        DependencyLoaderAccess.registerInstance(IMinecraftBukkitRuntimeState.class, new MinecraftBukkitRuntimeState(startedAtEpochMillis));
        com.tjxjnoobie.api.dependency.DependencyLoaderAccess.registerInstance(org.tavall.api.minecraft.frontend.IFrontendControlConfig.class,
                new FrontendControlConfig(controlIngressUri, "minecraft-bukkit-server"));
        new MinecraftBukkitServerDependencyModule().registerDependencies();
    }

    private ControlIngressFixture startControlIngressServer(Function<ControlPlaneTcpBridgeRequest, FrontendTcpControlBridgeResponse> responder) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("127.0.0.1", 0));
        AtomicReference<FrontendCommandEnvelope> submittedEnvelope = new AtomicReference<>();
        AtomicReference<String> rawRequestBody = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        ObjectMapper objectMapper = new JsonMapperProvider().mapper();
        Thread acceptThread = new Thread(() -> handleControlIngress(serverSocket, objectMapper, submittedEnvelope, rawRequestBody, failure, responder),
                "minecraft-bukkit-control-ingress-test");
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
            Function<ControlPlaneTcpBridgeRequest, FrontendTcpControlBridgeResponse> responder
    ) {
        try (
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            String requestBody = reader.readLine();
            rawRequestBody.set(requestBody);
            JsonNode root = objectMapper.readTree(requestBody);
            ControlPlaneTcpBridgeRequest request;
            if (root != null && root.hasNonNull("requestType")) {
                request = objectMapper.treeToValue(root, ControlPlaneTcpBridgeRequest.class);
            } else {
                FrontendCommandEnvelope envelope = objectMapper.treeToValue(root, FrontendCommandEnvelope.class);
                submittedEnvelope.set(envelope);
                request = ControlPlaneTcpBridgeRequest.frontendCommand(envelope);
            }
            if (request.frontendCommand() != null) {
                submittedEnvelope.set(request.frontendCommand());
            }

            FrontendTcpControlBridgeResponse response = responder.apply(request);
            writer.write(objectMapper.writeValueAsString(response));
            writer.newLine();
            writer.flush();
        } catch (Throwable exception) {
            if (!serverSocket.isClosed()) {
                failure.set(exception);
            }
        }
    }

    private FrontendCommandVerificationResult dispatched(FrontendCommandEnvelope envelope, String message) {
        Map<String, String> metadata = new LinkedHashMap<String, String>();
        String rawInput = envelope.rawInput();
        if (rawInput != null && !rawInput.isBlank()) {
            String controlConsoleInput = rawInput.startsWith("kd ") ? rawInput.substring(3) : rawInput;
            metadata.put("controlConsoleInput", controlConsoleInput);
        }
        return new FrontendCommandVerificationResult(
                envelope,
                FrontendCommandVerificationState.DISPATCHED,
                true,
                message,
                "cmd-bridge",
                "COMPLETED",
                metadata
        );
    }

    private InteractionResult interactionDispatched(InteractionRequest request, String message) {
        InteractionMenuModel menu = new InteractionMenuModel(
                "interaction-menu",
                "Interaction Menu",
                9,
                request.targetType(),
                request.targetId(),
                List.of(
                        new InteractionMenuElement(
                                "interaction-info",
                                4,
                                "PAPER",
                                "Interaction Ready",
                                List.of("The control plane approved this interaction."),
                                true,
                                null,
                                "",
                                Map.of()
                        )
                ),
                Map.of("requestType", "INTERACTION_REQUEST")
        );
        return new InteractionResult(
                request.requestId(),
                InteractionResultType.OPEN_MENU,
                true,
                message,
                menu,
                null,
                Map.of("requestType", "INTERACTION_REQUEST")
        );
    }

    private PlayerDataResponse playerData(PlayerDataRequest request) {
        return new PlayerDataResponse(
                request.requestId(),
                true,
                "Player profile loaded.",
                request.playerId(),
                request.playerName(),
                true,
                false,
                "miner@example.com",
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"),
                7,
                42,
                1337L,
                List.of(new PlayerPlatformBindingView(
                        "MINECRAFT",
                        "minecraft-player-1",
                        request.playerName(),
                        true,
                        Instant.parse("2025-01-01T00:00:00Z"),
                        Instant.parse("2025-01-02T00:00:00Z"),
                        Map.of("source", "test")
                )),
                Map.of("surfaceIdentity", "BUKKIT_SERVER")
        );
    }

    private int findAvailablePort() throws IOException {
        try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(0)) {
            return serverSocket.getLocalPort();
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
