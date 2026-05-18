package org.tavall.control.node;

import com.tjxjnoobie.api.dependency.DependencyLoaderAccess;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.guild.GuildJobTitle;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.InMemoryGuildRepository;
import org.tavall.control.guild.GuildKingdom;
import org.junit.jupiter.api.BeforeEach;
import org.tavall.control.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ResourceNodeMiddlewareIntegrationTest {
    @BeforeEach
    void clearDependencies() {
        DependencyLoaderAccess.clear();
    }

    @Test
    void productionTickStoresResourcesAndSkipsDepletedNodes() {
        IResourceNodeDomain nodeDomain = new IResourceNodeDomain() {
        };
        ResourceNodeRepository nodeRepository = nodeDomain.registerResourceNodeRepository(new InMemoryResourceNodeRepository());
        ResourceNodeCreationHandler creationHandler = nodeDomain.getResourceNodeCreationHandler();
        ResourceProductionTickHandler tickHandler = nodeDomain.getResourceProductionTickHandler();
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
        IResourceNodeDomain nodeDomain = new IResourceNodeDomain() {
        };
        ResourceNodeRepository nodeRepository = nodeDomain.registerResourceNodeRepository(new InMemoryResourceNodeRepository());
        nodeDomain.registerGuildRepository(new InMemoryGuildRepository());
        Instant now = Instant.parse("2026-04-30T13:00:00Z");
        UniversalPlayerId ruler = UniversalPlayerId.random();
        GuildKingdom guild = nodeDomain.getGuildCreationHandler().createGuildKingdom("Nodes", "NOD", ruler, now);
        UniversalPlayerId worker = UniversalPlayerId.random();
        GuildMemberProfile member = nodeDomain.getGuildMembershipHandler().addPlayerToGuild(guild.guildId(), worker, now);
        GuildMemberProfile quartermaster = nodeDomain.getGuildJobAssignmentHandler().assignJobTitle(guild.guildId(), worker, GuildJobTitle.QUARTERMASTER, now);
        ResourceNode node = nodeDomain.getResourceNodeCreationHandler()
                .createResourceNode(MiddlewareResourceType.FOOD, new CanonicalLocation("world", 1.0d, 64.0d, 1.0d), Optional.of(guild.guildId()), Optional.of(worker), 10);
        ResourceProductionTickHandler tickHandler = nodeDomain.getResourceProductionTickHandler();

        assertEquals(10, tickHandler.runProductionTick(node.nodeId(), member, 0.0d).producedAmount());
        assertEquals(10, tickHandler.runProductionTick(node.nodeId(), quartermaster, 0.0d).producedAmount());
    }
}
