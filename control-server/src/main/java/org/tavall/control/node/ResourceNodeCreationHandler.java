package org.tavall.control.node;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Optional;

public final class ResourceNodeCreationHandler implements ResourceNodeDomain {
    public ResourceNodeCreationHandler() {
    }

    public ResourceNodeCreationHandler(ResourceNodeRepository resourceNodeRepository) {
        registerResourceNodeRepository(resourceNodeRepository);
    }

    public ResourceNode createResourceNode(
            MiddlewareResourceType nodeType,
            CanonicalLocation location,
            Optional<GuildId> ownerGuildId,
            Optional<UniversalPlayerId> ownerPlayerId,
            int productionRate
    ) {
        ResourceNode node = new ResourceNode(
                ResourceNodeId.random(),
                nodeType,
                location,
                ownerGuildId,
                ownerPlayerId,
                productionRate,
                0,
                false,
                false,
                new GlobalAssetId("node." + nodeType.name().toLowerCase() + ".basic"),
                Map.of()
        );
        return getResourceNodeRepository().saveResourceNode(node);
    }
}
