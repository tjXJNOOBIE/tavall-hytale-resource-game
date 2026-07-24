package org.tavall.api.minecraft.backend.rank;

import org.junit.jupiter.api.Test;
import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.RankOperationType;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.permissions.UniversalPermissionRole;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RankApiTest {
    @Test
    void listInspectAndSetRoleUseTheRepository() {
        InMemoryRankRepository repository = new InMemoryRankRepository();
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        repository.saveRankDefinition(new RankDefinition("Member", 100, Set.of(), now, now));
        repository.saveRankDefinition(new RankDefinition("ADMIN", 80, Set.of("MANAGE_GAME_STATE"), now, now));
        repository.saveRankDefinition(new RankDefinition("OWNER", 100, Set.of(), now, now));
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
        assertTrue(listResponse.subjects().stream().map(subject -> subject.role()).toList().contains(UniversalPermissionRole.ADMIN));

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
        assertEquals(UniversalPermissionRole.MEMBER, inspectResponse.subject().role());

        RankResponse setResponse = api.inspect(
                RankRequest.setRole(
                        "rank-set",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "player-1",
                        "Miner",
                        UniversalPermissionRole.ADMIN,
                        Map.of("source", "test"),
                        3L
                ),
                now
        );

        assertTrue(setResponse.success());
        assertEquals(UniversalPermissionRole.ADMIN, setResponse.subject().role());

        RankResponse memberResponse = api.inspect(
                RankRequest.setRole(
                        "rank-member",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "player-1",
                        "Miner",
                        UniversalPermissionRole.MEMBER,
                        Map.of("source", "test"),
                        4L
                ),
                now
        );

        assertTrue(memberResponse.success());
        assertEquals(UniversalPermissionRole.MEMBER, memberResponse.subject().role());
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

        RankResponse missingRoleTarget = api.inspect(
                RankRequest.setRole(
                        "missing-role-target",
                        ResourceGameFrontendPlatform.MINECRAFT,
                        "admin-1",
                        "Admin",
                        "missing",
                        "Missing",
                        UniversalPermissionRole.ADMIN,
                        Map.of(),
                        6L
                ),
                now
        );

        assertFalse(missingRoleTarget.success());
        assertTrue(missingRoleTarget.message().contains("Player not found"));

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
