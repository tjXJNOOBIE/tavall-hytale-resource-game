package com.tavall.resourcegame.middleware.cloud;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CloudCoreRepositoryTest {
    @Test
    void joinTokensAreIndexedByHashAndConsumptionPreservesHashLookup() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        Instant now = Instant.parse("2026-05-08T15:00:00Z");
        JoinToken token = new JoinToken(UUID.randomUUID(), "hash-1", UUID.randomUUID(), now.plusSeconds(60),
                Optional.empty(), Optional.empty(), Optional.of("us-west"), Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT),
                Map.of());

        repository.saveJoinToken(token);
        repository.saveJoinToken(token.consumed(now));

        assertTrue(repository.findJoinTokenByHash("hash-1").orElseThrow().consumedAt().isPresent());
    }

    @Test
    void pendingCommandLookupOnlyMatchesOpenCommandsForSameNodeTypeAndCorrelation() {
        InMemoryCloudRepository repository = new InMemoryCloudRepository();
        Instant now = Instant.parse("2026-05-08T15:00:00Z");
        UUID nodeId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        repository.saveCommand(command(nodeId, CloudCommandType.START_WORKLOAD, CloudCommandStatus.CREATED, correlationId, now));
        repository.saveCommand(command(nodeId, CloudCommandType.START_WORKLOAD, CloudCommandStatus.SUCCEEDED, UUID.randomUUID(), now.plusSeconds(1)));

        assertTrue(repository.hasPendingCommand(nodeId, CloudCommandType.START_WORKLOAD, correlationId));
        assertEquals(1, repository.findPendingCommands(nodeId).size());
    }

    @Test
    void domainMetadataIsDefensivelyCopied() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("environment", "dev");

        CloudNode node = new CloudNode(UUID.randomUUID(), "node-1", "203.0.113.20", "10.0.0.20", "us-west",
                "dc-1", "owned", "test-cpu", 8, 16384, 12000, 250, 200, "linux", NodeArchitecture.X86_64,
                CloudNodeStatus.ONLINE, Set.of(CloudNodeCapability.CAN_RUN_MINECRAFT), Instant.now(), Instant.now(),
                Set.of("kingdom"), metadata);
        metadata.put("environment", "prod");

        assertEquals("dev", node.metadata().get("environment"));
        assertThrows(UnsupportedOperationException.class, () -> node.metadata().put("x", "y"));
    }

    private CloudCommand command(UUID nodeId, CloudCommandType type, CloudCommandStatus status, UUID correlationId, Instant now) {
        return new CloudCommand(UUID.randomUUID(), nodeId, type, "{}", UUID.randomUUID(), now, status, Optional.empty(),
                correlationId, Optional.empty(), Optional.empty(), Map.of());
    }
}
