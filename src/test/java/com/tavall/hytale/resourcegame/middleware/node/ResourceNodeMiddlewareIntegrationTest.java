package com.tavall.hytale.resourcegame.middleware.node;

import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobAssignmentHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobTitle;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMembershipHandler;
import com.tavall.hytale.resourcegame.middleware.guild.InMemoryGuildRepository;
import com.tavall.hytale.resourcegame.middleware.guild.GuildCreationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ResourceNodeMiddlewareIntegrationTest {
    @Test
    void productionTickStoresResourcesAndSkipsDepletedNodes() {
        InMemoryResourceNodeRepository nodeRepository = new InMemoryResourceNodeRepository();
        ResourceNodeCreationHandler creationHandler = new ResourceNodeCreationHandler(nodeRepository);
        ResourceProductionTickHandler tickHandler = new ResourceProductionTickHandler(nodeRepository, new GuildJobBuffCalculationHandler());
        ResourceNode node = creationHandler.createResourceNode(MiddlewareResourceType.IRON, new CanonicalLocation("world", 5.0d, 64.0d, 6.0d), Optional.empty(), Optional.empty(), 10);

        ResourceProductionTickResult result = tickHandler.runProductionTick(node.nodeId(), null, 0.25d);

        assertEquals(12, result.producedAmount());
        assertEquals(12, result.resourceNode().currentStoredAmount());

        nodeRepository.saveResourceNode(result.resourceNode().markDepleted());
        ResourceProductionTickResult depletedResult = tickHandler.runProductionTick(node.nodeId(), null, 1.0d);
        assertEquals(0, depletedResult.producedAmount());
    }

    @Test
    void resourceProductionJobBuffAppliesOnlyThroughAssignedJob() {
        InMemoryResourceNodeRepository nodeRepository = new InMemoryResourceNodeRepository();
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        Instant now = Instant.parse("2026-04-30T13:00:00Z");
        UniversalPlayerId ruler = UniversalPlayerId.random();
        GuildKingdom guild = new GuildCreationHandler(guildRepository).createGuildKingdom("Nodes", "NOD", ruler, now);
        UniversalPlayerId worker = UniversalPlayerId.random();
        GuildMemberProfile member = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guild.guildId(), worker, now);
        GuildMemberProfile quartermaster = new GuildJobAssignmentHandler(guildRepository).assignJobTitle(guild.guildId(), worker, GuildJobTitle.QUARTERMASTER, now);
        ResourceNode node = new ResourceNodeCreationHandler(nodeRepository)
                .createResourceNode(MiddlewareResourceType.FOOD, new CanonicalLocation("world", 1.0d, 64.0d, 1.0d), Optional.of(guild.guildId()), Optional.of(worker), 10);
        ResourceProductionTickHandler tickHandler = new ResourceProductionTickHandler(nodeRepository, new GuildJobBuffCalculationHandler());

        assertEquals(10, tickHandler.runProductionTick(node.nodeId(), member, 0.0d).producedAmount());
        assertEquals(10, tickHandler.runProductionTick(node.nodeId(), quartermaster, 0.0d).producedAmount());
    }
}
