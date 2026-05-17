package com.tavall.resourcegame.controlserver.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tavall.resourcegame.controlserver.ResourceGameControlServerModule;
import com.tavall.resourcegame.dependency.DependencyLoaderAccess;
import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.control.ControlCommandRuntime;
import com.tavall.resourcegame.middleware.identity.PlatformAccountBinding;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerAccount;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;
import com.tavall.resourcegame.shared.frontend.FrontendCommandEnvelope;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationResult;
import com.tavall.resourcegame.shared.frontend.FrontendCommandVerificationState;
import com.tavall.resourcegame.shared.frontend.InteractionRequest;
import com.tavall.resourcegame.shared.frontend.InteractionResult;
import com.tavall.resourcegame.shared.frontend.InteractionResultType;
import com.tavall.resourcegame.shared.frontend.InteractionTargetType;
import com.tavall.resourcegame.shared.frontend.PlayerDataRequest;
import com.tavall.resourcegame.shared.frontend.PlayerDataResponse;
import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.PunishResponse;
import com.tavall.resourcegame.shared.frontend.PunishOperationType;
import com.tavall.resourcegame.shared.frontend.PunishRecord;
import com.tavall.resourcegame.shared.frontend.RankRequest;
import com.tavall.resourcegame.shared.frontend.RankResponse;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import com.tavall.resourcegame.shared.permissions.UniversalPermissionRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ControlPlaneTcpBridgeServerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @AfterEach
    void clearDependencies() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void roundTripSubmitsFrontendCommandToControlRuntime() throws Exception {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        try (ControlPlaneTcpBridgeServer bridgeServer = ControlPlaneTcpBridgeServer.start(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        )) {
            ControlPlaneTcpBridgeClient client = new ControlPlaneTcpBridgeClient(
                    new ControlPlaneTcpBridgeConfiguration("127.0.0.1", bridgeServer.localPort())
            );
            FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-player-1",
                    "Miner",
                    "/kd tick run 1",
                    "corr-control-bridge-roundtrip",
                    Map.of("server", "kingdoms")
            );

            FrontendCommandVerificationResult result = client.submitCommand(envelope);

            assertEquals(FrontendCommandVerificationState.DISPATCHED, result.state());
            assertTrue(result.success());
            assertEquals("tick healing 1", result.metadata().get("controlConsoleInput"));
            assertNotNull(runtime.auditLogRepository().findRecentAuditLogs(1).stream().findFirst().orElse(null));
        }
    }

    @Test
    void roundTripSubmitsInteractionRequestToControlRuntime() throws Exception {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        try (ControlPlaneTcpBridgeServer bridgeServer = ControlPlaneTcpBridgeServer.start(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        )) {
            ControlPlaneTcpBridgeClient client = new ControlPlaneTcpBridgeClient(
                    new ControlPlaneTcpBridgeConfiguration("127.0.0.1", bridgeServer.localPort())
            );
            InteractionRequest request = new InteractionRequest(
                    "corr-interaction-bridge-roundtrip",
                    "minecraft-player-1",
                    InteractionTargetType.NPC,
                    "npc-1",
                    "open_menu",
                    "kingdoms-server",
                    "overworld",
                    Map.of(
                            "npcType", "farmer",
                            "displayName", "Farmer",
                            "attachedBuildingId", "farmstead-1",
                            "buildingType", "farmstead",
                            "buildingLevel", "2"
                    ),
                    1L
            );

            InteractionResult result = client.submitInteraction(request);

            assertEquals(InteractionResultType.OPEN_MENU, result.resultType());
            assertTrue(result.success());
            assertNotNull(result.menu());
            assertEquals(InteractionTargetType.BUILDING, result.menu().targetType());
            assertEquals("farmstead-1", result.menu().targetId());
            assertEquals("farmstead-1", result.menu().metadata().get("buildingId"));
        }
    }

    @Test
    void roundTripFetchesPlayerDataFromControlRuntime() throws Exception {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        UUID playerUuid = UUID.fromString("00000000-0000-0000-0000-000000000123");
        UniversalPlayerId universalPlayerId = UniversalPlayerId.of(playerUuid);
        runtime.accountRepository().saveAccount(new UniversalPlayerAccount(
                universalPlayerId,
                "Miner",
                Optional.of("miner@example.com"),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"),
                false,
                Map.of(
                        "accountLevel", "4",
                        "accountExperience", "25",
                        "accountTotalExperience", "1337"
                )
        ));
        runtime.platformAccountBindingRepository().savePlatformBinding(new PlatformAccountBinding(
                UUID.fromString("00000000-0000-0000-0000-000000000456"),
                universalPlayerId,
                GamePlatform.MINECRAFT,
                "minecraft-player-1",
                "Miner",
                true,
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"),
                Map.of("source", "test")
        ));
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        try (ControlPlaneTcpBridgeServer bridgeServer = ControlPlaneTcpBridgeServer.start(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        )) {
            ControlPlaneTcpBridgeClient client = new ControlPlaneTcpBridgeClient(
                    new ControlPlaneTcpBridgeConfiguration("127.0.0.1", bridgeServer.localPort())
            );
            PlayerDataRequest request = new PlayerDataRequest(
                    "corr-player-data-bridge-roundtrip",
                    playerUuid,
                    "Miner",
                    "kingdoms-server",
                    "overworld",
                    Map.of("surfaceIdentity", "BUKKIT_SERVER"),
                    2L
            );

            PlayerDataResponse result = client.fetchPlayerData(request);

            assertTrue(result.success());
            assertEquals("Player profile loaded.", result.message());
            assertEquals(playerUuid, result.playerId());
            assertEquals("Miner", result.displayName());
            assertTrue(result.accountExists());
            assertEquals(false, result.accountDisabled());
            assertEquals("miner@example.com", result.primaryEmail());
            assertEquals(4, result.accountLevel());
            assertEquals(25, result.accountExperience());
            assertEquals(1337L, result.accountTotalExperience());
            assertEquals(1, result.platformBindings().size());
            assertEquals("MINECRAFT", result.platformBindings().getFirst().platform());
            assertEquals("minecraft-player-1", result.platformBindings().getFirst().platformAccountId());
        }
    }

    @Test
    void roundTripListsAndUpdatesRankSubjectsFromControlRuntime() throws Exception {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        try (ControlPlaneTcpBridgeServer bridgeServer = ControlPlaneTcpBridgeServer.start(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        )) {
            ControlPlaneTcpBridgeClient client = new ControlPlaneTcpBridgeClient(
                    new ControlPlaneTcpBridgeConfiguration("127.0.0.1", bridgeServer.localPort())
            );

            RankResponse listResponse = client.submitRankRequest(RankRequest.list(
                    "corr-rank-list",
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-player-1",
                    "Miner",
                    Map.of("server", "kingdoms"),
                    3L
            ));

            assertTrue(listResponse.success());
            assertTrue(listResponse.subjects().size() >= 2);

            RankResponse updateResponse = client.submitRankRequest(RankRequest.setRole(
                    "corr-rank-update",
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-player-1",
                    "Miner",
                    "00000000-0000-0000-0000-000000000999",
                    "Ranked Miner",
                    UniversalPermissionRole.MODERATOR,
                    Map.of("server", "kingdoms"),
                    4L
            ));

            assertTrue(updateResponse.success());
            assertEquals("Ranked Miner", updateResponse.subject().displayName());
            assertEquals(UniversalPermissionRole.MODERATOR, updateResponse.subject().role());

            RankResponse inspectResponse = client.submitRankRequest(RankRequest.inspect(
                    "corr-rank-inspect",
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-player-1",
                    "Miner",
                    "00000000-0000-0000-0000-000000000999",
                    "Ranked Miner",
                    Map.of("server", "kingdoms"),
                    5L
            ));

            assertTrue(inspectResponse.success());
            assertEquals("Ranked Miner", inspectResponse.subject().displayName());
            assertEquals(UniversalPermissionRole.MODERATOR, inspectResponse.subject().role());
        }
    }

    @Test
    void roundTripRecordsAndInspectsPunishmentsFromControlRuntime() throws Exception {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        try (ControlPlaneTcpBridgeServer bridgeServer = ControlPlaneTcpBridgeServer.start(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        )) {
            ControlPlaneTcpBridgeClient client = new ControlPlaneTcpBridgeClient(
                    new ControlPlaneTcpBridgeConfiguration("127.0.0.1", bridgeServer.localPort())
            );

            PunishResponse banResponse = client.submitPunishRequest(PunishRequest.ban(
                    "corr-punish-ban",
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-admin-1",
                    "MinerAdmin",
                    "00000000-0000-0000-0000-000000000abc",
                    "MinerOne",
                    "1h",
                    "Testing",
                    Map.of("server", "kingdoms"),
                    3L
            ));

            assertTrue(banResponse.success());
            assertEquals(PunishOperationType.BAN, banResponse.activePunishment().operation());
            assertEquals("MinerOne", banResponse.activePunishment().targetDisplayName());

            PunishResponse inspectResponse = client.submitPunishRequest(PunishRequest.inspect(
                    "corr-punish-inspect",
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-admin-1",
                    "MinerAdmin",
                    "00000000-0000-0000-0000-000000000abc",
                    "MinerOne",
                    Map.of("server", "kingdoms"),
                    4L
            ));

            assertTrue(inspectResponse.success());
            assertNotNull(inspectResponse.activePunishment());
            assertEquals(PunishOperationType.BAN, inspectResponse.activePunishment().operation());
            assertEquals("MinerOne", inspectResponse.activePunishment().targetDisplayName());

            PunishResponse unbanResponse = client.submitPunishRequest(PunishRequest.unban(
                    "corr-punish-unban",
                    ResourceGameFrontendPlatform.MINECRAFT,
                    "minecraft-admin-1",
                    "MinerAdmin",
                    "00000000-0000-0000-0000-000000000abc",
                    "MinerOne",
                    Map.of("server", "kingdoms"),
                    5L
            ));

            assertTrue(unbanResponse.success());
            assertEquals(PunishOperationType.UNBAN, unbanResponse.activePunishment().operation());
        }
    }

    @Test
    void offlineBridgeReturnsRejectedResult() {
        ControlPlaneTcpBridgeClient client = new ControlPlaneTcpBridgeClient(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        );
        FrontendCommandEnvelope envelope = FrontendCommandEnvelope.command(
                ResourceGameFrontendPlatform.MINECRAFT,
                "minecraft-player-2",
                "Miner",
                "/kd tick run 1",
                "corr-control-bridge-offline",
                Map.of()
        );

        FrontendCommandVerificationResult result = client.submitCommand(envelope);

        assertEquals(FrontendCommandVerificationState.REJECTED, result.state());
        assertTrue(result.message().contains("Control bridge unavailable"));
    }

    @Test
    void invalidMessageReturnsTypedErrorResponse() throws Exception {
        ControlCommandRuntime runtime = new ResourceGameControlServerModule().createInMemoryRuntime();
        DependencyLoaderAccess.registerInstance(ControlCommandRuntime.class, runtime);
        try (ControlPlaneTcpBridgeServer bridgeServer = ControlPlaneTcpBridgeServer.start(
                new ControlPlaneTcpBridgeConfiguration("127.0.0.1", 0)
        );
             Socket socket = new Socket("127.0.0.1", bridgeServer.localPort());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            writer.write("{not-json");
            writer.newLine();
            writer.flush();

            String responseJson = reader.readLine();
            assertNotNull(responseJson);

            ControlPlaneTcpBridgeResponse response = OBJECT_MAPPER.readValue(responseJson, ControlPlaneTcpBridgeResponse.class);
            assertNull(response.verificationResult());
            assertTrue(response.errorMessage().contains("Invalid control bridge request"));
        }
    }
}
