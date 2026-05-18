package org.tavall.control.node;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.common.CanonicalLocation;
import org.tavall.control.common.MetadataMaps;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ResourceNode(
        ResourceNodeId nodeId,
        MiddlewareResourceType nodeType,
        CanonicalLocation location,
        Optional<GuildId> ownerGuildId,
        Optional<UniversalPlayerId> ownerPlayerId,
        int productionRate,
        int currentStoredAmount,
        boolean contested,
        boolean depleted,
        GlobalAssetId globalAssetId,
        Map<String, String> metadata
) {
    public ResourceNode {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(nodeType, "nodeType");
        Objects.requireNonNull(location, "location");
        ownerGuildId = ownerGuildId == null ? Optional.empty() : ownerGuildId;
        ownerPlayerId = ownerPlayerId == null ? Optional.empty() : ownerPlayerId;
        productionRate = Math.max(0, productionRate);
        currentStoredAmount = Math.max(0, currentStoredAmount);
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        metadata = MetadataMaps.immutable(metadata);
    }

    public ResourceNode withCurrentStoredAmount(int amount) {
        return new ResourceNode(nodeId, nodeType, location, ownerGuildId, ownerPlayerId, productionRate, amount, contested, depleted, globalAssetId, metadata);
    }

    public ResourceNode markDepleted() {
        return new ResourceNode(nodeId, nodeType, location, ownerGuildId, ownerPlayerId, productionRate, currentStoredAmount, contested, true, globalAssetId, metadata);
    }
}
