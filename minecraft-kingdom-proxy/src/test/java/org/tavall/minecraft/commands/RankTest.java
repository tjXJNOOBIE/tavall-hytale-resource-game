package org.tavall.minecraft.commands;

import org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.permissions.RankSubject;
import org.tavall.dependency.DependencyLoader;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.minecraft.bootstrap.VelocityDependencyModule;
import org.tavall.minecraft.bootstrap.VelocityProxyConfig;
import org.tavall.minecraft.commands.source.TestVelocityCommandSource;
import org.tavall.minecraft.commands.support.VelocityCommandResult;
import org.tavall.minecraft.permissions.IRankControlBridgeClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RankTest {
    @BeforeEach
    void clearDependencies() {
        DependencyLoader.getDependencyLoader().clear();
        DependencyLoaderAccess.clear();
    }

    @AfterEach
    void clearDependenciesAfterTest() {
        DependencyLoader.getDependencyLoader().clear();
        DependencyLoaderAccess.clear();
    }

    @Test
    void listInspectSetAndRemoveRouteThroughBridgeClient() {
        RecordingRankControlBridgeClient bridgeClient = new RecordingRankControlBridgeClient();
        bridgeClient.enqueue(RankResponse.listed(
                "rank-list",
                "Loaded 3 rank definitions.",
                java.util.List.of(
                        new RankSubject("Member", "Member", "Member", 100, Set.of(), Map.of()),
                        new RankSubject("VIP+", "VIP+", "VIP+", 250, Set.of(), Map.of())
                ),
                Map.of()
        ));
        bridgeClient.enqueue(RankResponse.inspected(
                "rank-inspect",
                "Loaded rank profile for Miner.",
                new RankSubject("player-1", "Miner", "Member", 100, Set.of(), Map.of()),
                Map.of()
        ));
        bridgeClient.enqueue(RankResponse.updated(
                "rank-set",
                "Updated Miner to God.",
                new RankSubject("player-1", "Miner", "God", 1000, Set.of("speedrun.*"), Map.of()),
                java.util.List.of(),
                Map.of()
        ));
        bridgeClient.enqueue(RankResponse.updated(
                "rank-remove",
                "Reverted Miner to Member.",
                new RankSubject("player-1", "Miner", "Member", 100, Set.of(), Map.of()),
                java.util.List.of(),
                Map.of()
        ));

        VelocityProxyConfig config = new VelocityProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-rank-test",
                Map.of(),
                Set.of(),
                Set.of(),
                true
        );
        new VelocityDependencyModule().registerDependencies(config, bridgeClient);
        Rank rank = (Rank) DependencyLoaderAccess.findInstance(IRank.class);
        TestVelocityCommandSource member = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.command"));
        TestVelocityCommandSource admin = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.admin"));

        VelocityCommandResult listResult = rank.execute(member, "rank", new String[]{"list"});
        VelocityCommandResult inspectResult = rank.execute(member, "rank", new String[]{"inspect", "Miner"});
        VelocityCommandResult setResult = rank.execute(admin, "rank", new String[]{"set", "Miner", "God"});
        VelocityCommandResult removeResult = rank.execute(admin, "rank", new String[]{"remove", "Miner"});

        assertTrue(listResult.success());
        assertTrue(listResult.message().contains("Member(100)"));
        assertTrue(inspectResult.success());
        assertTrue(inspectResult.message().contains("Miner -> Member"));
        assertTrue(setResult.success());
        assertTrue(setResult.message().contains("Miner -> God"));
        assertTrue(removeResult.success());
        assertTrue(removeResult.message().contains("Miner -> Member"));

        assertEquals("LIST", bridgeClient.requests().get(0).operation().name());
        assertEquals("Miner", bridgeClient.requests().get(3).targetDisplayName());
    }

    @Test
    void setRequiresAdminPermission() {
        RecordingRankControlBridgeClient bridgeClient = new RecordingRankControlBridgeClient();
        VelocityProxyConfig config = new VelocityProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-rank-test",
                Map.of(),
                Set.of(),
                Set.of(),
                true
        );
        new VelocityDependencyModule().registerDependencies(config, bridgeClient);
        Rank rank = (Rank) DependencyLoaderAccess.findInstance(IRank.class);
        TestVelocityCommandSource member = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.command"));

        VelocityCommandResult denied = rank.execute(member, "rank", new String[]{"set", "Miner", "God"});

        assertFalse(denied.success());
        assertTrue(denied.message().contains("Missing permission"));
    }

    private static final class RecordingRankControlBridgeClient implements IRankControlBridgeClient {
        private final Deque<RankResponse> responses = new ArrayDeque<>();
        private final AtomicReference<RankRequest> lastRequest = new AtomicReference<>();
        private final List<RankRequest> requests = new ArrayList<>();

        void enqueue(RankResponse response) {
            responses.addLast(response);
        }

        RankRequest lastRequest() {
            return lastRequest.get();
        }

        List<RankRequest> requests() {
            return List.copyOf(requests);
        }

        @Override
        public RankResponse submitRankRequest(RankRequest request) {
            lastRequest.set(request);
            requests.add(request);
            return responses.isEmpty()
                    ? RankResponse.unavailable(request.requestId(), "No stubbed response available.")
                    : responses.removeFirst();
        }
    }
}
