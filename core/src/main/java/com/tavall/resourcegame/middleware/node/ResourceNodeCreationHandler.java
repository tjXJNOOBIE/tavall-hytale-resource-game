package com.tavall.resourcegame.middleware.node;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Optional;

public final class ResourceNodeCreationHandler implements IResourceNodeDomain {
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
