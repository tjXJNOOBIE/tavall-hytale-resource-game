package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.asset.ResolvedPlatformAsset;
import com.tavall.hytale.resourcegame.middleware.castle.Castle;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.node.ResourceNode;
import com.tavall.hytale.resourcegame.middleware.petition.Petition;
import com.tavall.hytale.resourcegame.middleware.petition.PropagandaCampaign;
import com.tavall.hytale.resourcegame.middleware.trade.TradeRoute;
import com.tavall.hytale.resourcegame.middleware.troop.Troop;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class FrontendProjectionHandler {
    private final GlobalAssetProjectionHandler globalAssetProjectionHandler;

    public FrontendProjectionHandler(GlobalAssetProjectionHandler globalAssetProjectionHandler) {
        this.globalAssetProjectionHandler = globalAssetProjectionHandler;
    }

    public FrontendProjection projectCastle(Castle castle, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, castle.castleId().toString(), ProjectionObjectType.CASTLE, castle.globalAssetId(), "Castle level " + castle.level(), Optional.of(castle.location()), castle.state().name(), actions, Map.of("ownerPlayerId", castle.ownerPlayerId().toString()));
    }

    public FrontendProjection projectResourceNode(ResourceNode node, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, node.nodeId().toString(), ProjectionObjectType.RESOURCE_NODE, node.globalAssetId(), node.nodeType().name(), Optional.of(node.location()), node.depleted() ? "DEPLETED" : "ACTIVE", actions, Map.of("storedAmount", Integer.toString(node.currentStoredAmount())));
    }

    public FrontendProjection projectGuildSummary(GuildKingdom guildKingdom, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, guildKingdom.guildId().toString(), ProjectionObjectType.GUILD, new GlobalAssetId("guild.banner.default"), guildKingdom.name(), Optional.empty(), guildKingdom.state().name(), actions, Map.of("tag", guildKingdom.tag()));
    }

    public FrontendProjection projectPetition(Petition petition, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, petition.petitionId().toString(), ProjectionObjectType.PETITION, new GlobalAssetId("petition.active"), petition.petitionType().name(), Optional.empty(), petition.state().name(), actions, Map.of("funding", Long.toString(petition.fundingAmount())));
    }

    public FrontendProjection projectPropagandaCampaign(PropagandaCampaign campaign, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, campaign.campaignId().value().toString(), ProjectionObjectType.PROPAGANDA_CAMPAIGN, new GlobalAssetId("petition.active"), campaign.targetScope(), Optional.empty(), "ACTIVE", actions, Map.of("reachScore", Double.toString(campaign.reachScore())));
    }

    public FrontendProjection projectTroop(Troop troop, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, troop.troopId().value().toString(), ProjectionObjectType.TROOP, troop.globalAssetId(), troop.troopType(), Optional.of(troop.location()), troop.status().name(), actions, Map.of("tier", Integer.toString(troop.tier())));
    }

    public FrontendProjection projectTradeRoute(TradeRoute tradeRoute, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, tradeRoute.routeId().value().toString(), ProjectionObjectType.TRADE_ROUTE, tradeRoute.globalAssetId(), "Trade route", Optional.empty(), tradeRoute.state().name(), actions, Map.of("travelProgress", Double.toString(tradeRoute.travelProgress())));
    }

    private FrontendProjection projection(
            GamePlatform platform,
            String canonicalObjectId,
            ProjectionObjectType objectType,
            GlobalAssetId globalAssetId,
            String displayName,
            Optional<com.tavall.hytale.resourcegame.middleware.common.CanonicalLocation> location,
            String state,
            List<InteractionAction> actions,
            Map<String, String> metadata
    ) {
        ResolvedPlatformAsset resolvedAsset = globalAssetProjectionHandler.resolveAssetForProjection(globalAssetId, platform);
        return new FrontendProjection(
                UUID.randomUUID(),
                platform,
                canonicalObjectId,
                objectType,
                globalAssetId,
                resolvedAsset.platformAssetReference(),
                displayName,
                location,
                state,
                actions,
                withFallback(metadata, resolvedAsset.fallbackAssetKey())
        );
    }

    private Map<String, String> withFallback(Map<String, String> metadata, String fallbackAssetKey) {
        java.util.HashMap<String, String> copy = new java.util.HashMap<>(metadata);
        copy.put("fallbackAssetKey", fallbackAssetKey);
        return Map.copyOf(copy);
    }
}
