package org.tavall.minecraft.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tavall.api.minecraft.backend.rank.InMemoryRankRepository;
import org.tavall.api.minecraft.backend.rank.RankDefinition;
import org.tavall.api.minecraft.backend.rank.RankPlayerProfile;
import org.tavall.dependency.DependencyLoader;
import org.tavall.dependency.DependencyLoaderAccess;
import org.tavall.minecraft.bootstrap.VelocityDependencyModule;
import org.tavall.minecraft.bootstrap.VelocityProxyConfig;
import org.tavall.minecraft.commands.source.TestVelocityCommandSource;
import org.tavall.minecraft.commands.support.VelocityCommandResult;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    void listInspectSetAndRemoveOperateDirectlyAgainstTheRepository() {
        InMemoryRankRepository repository = seededRepository();
        VelocityProxyConfig config = new VelocityProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-rank-test",
                Map.of(),
                Set.of(),
                Set.of(),
                true
        );
        new VelocityDependencyModule().registerDependencies(config, repository);
        Rank rank = (Rank) DependencyLoaderAccess.findInstance(IRank.class);

        TestVelocityCommandSource member = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.command"));
        TestVelocityCommandSource admin = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin"));
        TestVelocityCommandSource console = TestVelocityCommandSource.console(Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin"));

        VelocityCommandResult listResult = rank.execute(member, "rank", new String[]{"list"});
        VelocityCommandResult inspectResult = rank.execute(member, "rank", new String[]{"inspect", "Miner"});
        VelocityCommandResult consoleListResult = rank.execute(console, "rank", new String[]{"list"});
        VelocityCommandResult setResult = rank.execute(admin, "rank", new String[]{"set", "Miner", "God"});
        VelocityCommandResult removeResult = rank.execute(admin, "rank", new String[]{"remove", "Miner"});

        assertTrue(listResult.success());
        assertTrue(listResult.message().contains("Member(100)"));
        assertTrue(inspectResult.success());
        assertTrue(inspectResult.message().contains("Miner -> Member"));
        assertTrue(consoleListResult.success());
        assertTrue(consoleListResult.message().contains("VIP+"));
        assertTrue(setResult.success());
        assertTrue(setResult.message().contains("Miner -> God"));
        assertTrue(removeResult.success());
        assertTrue(removeResult.message().contains("Miner -> Member"));

        assertEquals("Member", repository.findPlayerProfileByDisplayName("Miner").orElseThrow().rankName());
    }

    @Test
    void setRequiresAdminPermission() {
        InMemoryRankRepository repository = seededRepository();
        VelocityProxyConfig config = new VelocityProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-rank-test",
                Map.of(),
                Set.of(),
                Set.of(),
                true
        );
        new VelocityDependencyModule().registerDependencies(config, repository);
        Rank rank = (Rank) DependencyLoaderAccess.findInstance(IRank.class);
        TestVelocityCommandSource member = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.command"));

        VelocityCommandResult denied = rank.execute(member, "rank", new String[]{"set", "Miner", "God"});

        assertFalse(denied.success());
        assertTrue(denied.message().contains("Missing permission"));
    }

    @Test
    void missingPlayerAndRankReturnErrors() {
        InMemoryRankRepository repository = seededRepository();
        VelocityProxyConfig config = new VelocityProxyConfig(
                "tavall.resourcegame.command",
                "tavall.resourcegame.admin",
                "velocity-rank-test",
                Map.of(),
                Set.of(),
                Set.of(),
                true
        );
        new VelocityDependencyModule().registerDependencies(config, repository);
        Rank rank = (Rank) DependencyLoaderAccess.findInstance(IRank.class);
        TestVelocityCommandSource admin = TestVelocityCommandSource.player("Miner", Set.of("tavall.resourcegame.command", "tavall.resourcegame.admin"));

        VelocityCommandResult missingPlayer = rank.execute(admin, "rank", new String[]{"inspect", "Missing"});
        VelocityCommandResult missingRank = rank.execute(admin, "rank", new String[]{"set", "Miner", "NotARank"});

        assertFalse(missingPlayer.success());
        assertTrue(missingPlayer.message().contains("Player not found"));
        assertFalse(missingRank.success());
        assertTrue(missingRank.message().contains("Rank does not exist"));
    }

    private InMemoryRankRepository seededRepository() {
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
        return repository;
    }
}
