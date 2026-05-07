package com.tavall.hytale.resourcegame.middleware.node;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation;
import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Optional;

public final class ResourceNodeCreationHandler {
    private final ResourceNodeRepository resourceNodeRepository;

    public ResourceNodeCreationHandler(ResourceNodeRepository resourceNodeRepository) {
        this.resourceNodeRepository = resourceNodeRepository;
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
        return resourceNodeRepository.saveResourceNode(node);
    }
}
