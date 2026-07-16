package org.tavall.api.minecraft.backend.rank;

import org.junit.jupiter.api.Test;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.RankOperationType;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RankApiTest {
    @Test
    void listInspectSetAndRemoveUseTheRepository() {
        InMemoryRankRepository repository = new InMemoryRankRepository();
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        repository.saveRankDefinition(new RankDefinition("Member", 100, Set.of(), now, now));
        repository.saveRankDefinition(new RankDefinition("VIP+", 250, Set.of("speedrun.*"), now, now));
        repository.saveRankDefinition(new RankDefinition("God", 1000, Set.of(), now, now));
        repository.savePlayerProfile(new RankPlayerProfile(
                "player-1",
                "Miner",
                "Member",
                100,
                Set.of(),
                Map.of("source", "test"),
                now,
                now
        ));
        RankApi api = new RankApi(repository);

        RankResponse listResponse = api.inspect(
                RankRequest.list(
                        "rank-list",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        Map.of("server", "kingdoms"),
                        1L
                ),
                now
        );

        assertTrue(listResponse.success());
        assertEquals(3, listResponse.subjects().size());
        assertTrue(listResponse.subjects().stream().map(subject -> subject.rankName()).toList().contains("VIP+"));

        RankResponse inspectResponse = api.inspect(
                RankRequest.inspect(
                        "rank-inspect",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "player-1",
                        "Miner",
                        Map.of(),
                        2L
                ),
                now
        );

        assertTrue(inspectResponse.success());
        assertNotNull(inspectResponse.subject());
        assertEquals("Member", inspectResponse.subject().rankName());

        RankResponse setResponse = api.inspect(
                RankRequest.setRank(
                        "rank-set",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "player-1",
                        "Miner",
                        "VIP+",
                        Map.of("source", "test"),
                        3L
                ),
                now
        );

        assertTrue(setResponse.success());
        assertEquals("VIP+", setResponse.subject().rankName());
        assertEquals(250, setResponse.subject().powerLevel());
        assertTrue(setResponse.subject().hasPermission("speedrun.fly"));

        RankResponse removeResponse = api.inspect(
                RankRequest.removeRank(
                        "rank-remove",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "player-1",
                        "Miner",
                        null,
                        Map.of("source", "test"),
                        4L
                ),
                now
        );

        assertTrue(removeResponse.success());
        assertEquals("Member", removeResponse.subject().rankName());
        assertFalse(removeResponse.subject().hasPermission("speedrun.fly"));
    }

    @Test
    void missingRankAndPlayerReturnUnavailableMessages() {
        InMemoryRankRepository repository = new InMemoryRankRepository();
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        repository.saveRankDefinition(new RankDefinition("Member", 100, Set.of(), now, now));
        RankApi api = new RankApi(repository);

        RankResponse missingPlayer = api.inspect(
                RankRequest.inspect(
                        "missing-player",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "missing",
                        "Missing",
                        Map.of(),
                        5L
                ),
                now
        );

        assertFalse(missingPlayer.success());
        assertTrue(missingPlayer.message().contains("Player not found"));

        RankResponse missingRank = api.inspect(
                RankRequest.setRank(
                        "missing-rank",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "missing",
                        "Missing",
                        "Nope",
                        Map.of(),
                        6L
                ),
                now
        );

        assertFalse(missingRank.success());
        assertTrue(missingRank.message().contains("Rank not found"));

        assertEquals(RankOperationType.INSPECT, RankRequest.inspect(
                "round-trip",
                ResourceGameFrontendPlatform.MINECRAFT,
                "admin-1",
                "Admin",
                "missing",
                "Missing",
                Map.of(),
                7L
        ).operation());
    }
}
