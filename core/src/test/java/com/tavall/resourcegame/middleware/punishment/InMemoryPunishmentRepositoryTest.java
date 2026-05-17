package com.tavall.resourcegame.middleware.punishment;

import com.tavall.resourcegame.shared.frontend.PunishOperationType;
import com.tavall.resourcegame.shared.frontend.PunishRecord;
import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.PunishResponse;
import com.tavall.resourcegame.shared.frontend.ResourceGameFrontendPlatform;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InMemoryPunishmentRepositoryTest {
    @Test
    void banWarnAndUnbanLifecycleUpdatesCanonicalPunishmentState() {
        InMemoryPunishmentRepository repository = new InMemoryPunishmentRepository();
        Instant now = Instant.parse("2026-05-14T12:00:00Z");
        PunishRequest banRequest = PunishRequest.ban(
                "punish-ban-1",
                ResourceGameFrontendPlatform.MINECRAFT,
                "admin-1",
                "Admin",
                "miner-1",
                "Miner",
                "1h",
                "Testing ban flow",
                Map.of("surfaceIdentity", "VELOCITY_PROXY"),
                now.toEpochMilli()
        );

        PunishResponse banResponse = repository.inspect(banRequest, now);
        assertTrue(banResponse.success());
        assertNotNull(banResponse.activePunishment());
        assertEquals(PunishOperationType.BAN, banResponse.activePunishment().operation());
        assertEquals("Testing ban flow", banResponse.activePunishment().reason());
        assertFalse(banResponse.activePunishment().durationText().isBlank());

        PunishResponse warnResponse = repository.inspect(
                PunishRequest.warn(
                        "punish-warn-1",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "miner-1",
                        "Miner",
                        "Repeated chat spam",
                        Map.of("surfaceIdentity", "VELOCITY_PROXY"),
                        now.plusSeconds(1).toEpochMilli()
                ),
                now.plusSeconds(1)
        );
        assertTrue(warnResponse.success());
        assertEquals("1", warnResponse.metadata().get("warnCount"));

        PunishResponse unbanResponse = repository.inspect(
                PunishRequest.unban(
                        "punish-unban-1",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "miner-1",
                        "Miner",
                        Map.of("surfaceIdentity", "VELOCITY_PROXY"),
                        now.plusSeconds(2).toEpochMilli()
                ),
                now.plusSeconds(2)
        );
        assertTrue(unbanResponse.success());
        assertNotNull(unbanResponse.activePunishment());
        assertEquals(PunishOperationType.UNBAN, unbanResponse.activePunishment().operation());

        PunishResponse inspectResponse = repository.inspect(
                PunishRequest.inspect(
                        "punish-inspect-1",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "miner-1",
                        "Miner",
                        Map.of("surfaceIdentity", "VELOCITY_PROXY"),
                        now.plusSeconds(3).toEpochMilli()
                ),
                now.plusSeconds(3)
        );
        assertTrue(inspectResponse.success());
        assertNull(inspectResponse.activePunishment());
        assertEquals(3, inspectResponse.punishments().size());
    }

    @Test
    void nullRequestReturnsUnavailableResponse() {
        InMemoryPunishmentRepository repository = new InMemoryPunishmentRepository();

        PunishResponse response = repository.inspect(null, Instant.now());

        assertFalse(response.success());
        assertEquals("Punish request was null.", response.message());
        assertTrue(response.punishments().isEmpty());
    }
}
