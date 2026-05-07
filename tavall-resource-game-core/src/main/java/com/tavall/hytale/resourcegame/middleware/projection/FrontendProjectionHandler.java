package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.asset.ResolvedPlatformAsset;
import com.tavall.hytale.resourcegame.middleware.castle.Castle;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenDisplayAnchorProjection;
import com.tavall.hytale.resourcegame.middleware.citizen.CitizenPopulationProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomClockProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomScheduleProjection;
import com.tavall.hytale.resourcegame.middleware.clock.KingdomTimePhase;
import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.kingdom.UniversalKingdomSimulationSystem;
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

    public FrontendProjection projectUniversalKingdom(UniversalKingdomSimulationSystem.UniversalKingdom kingdom, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, kingdom.kingdomId().value(), ProjectionObjectType.KINGDOM, new GlobalAssetId(globalAssetForKingdom(kingdom)), kingdom.displayName(), Optional.empty(), kingdom.state().name(), actions, Map.of(
                "folderName", kingdom.folderName(),
                "worldId", kingdom.worldId(),
                "currentPlayers", Integer.toString(kingdom.populationStats().currentPlayers()),
                "routingProfileId", kingdom.instanceRoutingProfileId()
        ));
    }

    public FrontendProjection projectKingdomBorder(UniversalKingdomSimulationSystem.KingdomBorderDefinition border, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, border.borderDefinitionId(), ProjectionObjectType.KINGDOM_BORDER, new GlobalAssetId("kingdom.border.default"), border.kingdomId().value() + " border", Optional.empty(), border.borderShape().name(), actions, Map.of(
                "kingdomId", border.kingdomId().value(),
                "worldId", border.worldId(),
                "minX", Double.toString(border.minX()),
                "maxX", Double.toString(border.maxX()),
                "minZ", Double.toString(border.minZ()),
                "maxZ", Double.toString(border.maxZ())
        ));
    }

    public FrontendProjection projectPlayerKingdomLocation(UniversalKingdomSimulationSystem.PlayerKingdomLocation location, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, location.universalPlayerId(), ProjectionObjectType.PLAYER_KINGDOM_LOCATION, new GlobalAssetId("coordinate.debug.marker"), location.universalPlayerId(), Optional.empty(), location.currentKingdomId().value(), actions, Map.of(
                "kingdomId", location.currentKingdomId().value(),
                "coordinate", location.canonicalCoordinate().compact(),
                "sourcePlatform", location.platform().name()
        ));
    }

    public FrontendProjection projectInstanceSwitchRequest(UniversalKingdomSimulationSystem.InstanceSwitchRequest request, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, request.switchRequestId(), ProjectionObjectType.INSTANCE_SWITCH_REQUEST, new GlobalAssetId("kingdom.transition.border_crossing"), request.universalPlayerId(), Optional.empty(), request.state().name(), actions, Map.of(
                "fromKingdomId", request.fromKingdomId().value(),
                "toKingdomId", request.toKingdomId().value(),
                "toInstanceId", request.toInstanceId(),
                "reason", request.reason().name()
        ));
    }

    public FrontendProjection projectKingdomClock(KingdomClockProjection clockProjection, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, "kingdom-clock:" + clockProjection.kingdomId(), ProjectionObjectType.KINGDOM_CLOCK, new GlobalAssetId(globalAssetForClockPhase(clockProjection.currentPhase())),
                clockProjection.kingdomId() + " clock", Optional.empty(), clockProjection.currentPhase().name(), actions, Map.ofEntries(
                        Map.entry("kingdomId", clockProjection.kingdomId()),
                        Map.entry("currentKingdomDay", Long.toString(clockProjection.currentKingdomDay())),
                        Map.entry("currentHour", Integer.toString(clockProjection.currentHour())),
                        Map.entry("currentMinute", Integer.toString(clockProjection.currentMinute())),
                        Map.entry("currentPhase", clockProjection.currentPhase().name()),
                        Map.entry("isDay", Boolean.toString(clockProjection.isDay())),
                        Map.entry("isNight", Boolean.toString(clockProjection.isNight())),
                        Map.entry("mode", clockProjection.mode().name()),
                        Map.entry("timezoneId", clockProjection.timezoneId()),
                        Map.entry("visualMood", clockProjection.visualMood().name()),
                        Map.entry("canonicalOwner", "plain-java-control-server")
                ));
    }

    public FrontendProjection projectKingdomSchedule(KingdomScheduleProjection scheduleProjection, GamePlatform platform, List<InteractionAction> actions) {
        return projection(platform, "kingdom-schedule:" + scheduleProjection.kingdomId(), ProjectionObjectType.KINGDOM_SCHEDULE, new GlobalAssetId("kingdom_schedule.shop.open"),
                scheduleProjection.kingdomId() + " schedule", Optional.empty(), "ACTIVE_WINDOWS_" + scheduleProjection.activeWindows().size(), actions, Map.of(
                        "kingdomId", scheduleProjection.kingdomId(),
                        "activeWindowCount", Integer.toString(scheduleProjection.activeWindows().size()),
                        "citizenScheduleHints", String.join(",", scheduleProjection.citizenScheduleHints()),
                        "shopOpenCloseHints", String.join(",", scheduleProjection.shopOpenCloseHints()),
                        "interiorMoodHints", String.join(",", scheduleProjection.interiorMoodHints()),
                        "troopTrainingHints", String.join(",", scheduleProjection.troopTrainingHints()),
                        "buildingProductivityHints", String.join(",", scheduleProjection.buildingProductivityHints()),
                        "moraleHints", String.join(",", scheduleProjection.moraleHints()),
                        "canonicalOwner", "plain-java-control-server"
                ));
    }

    public FrontendProjection projectCitizenPopulation(CitizenPopulationProjection citizenProjection, GamePlatform platform, List<InteractionAction> actions) {
        java.util.LinkedHashMap<String, String> metadata = new java.util.LinkedHashMap<>(citizenProjection.metadata());
        metadata.put("scopeType", citizenProjection.scope().scopeType().name());
        metadata.put("scopeId", citizenProjection.scope().scopeId());
        metadata.put("totalCitizens", Integer.toString(citizenProjection.totalCitizens()));
        metadata.put("totalTroops", Integer.toString(citizenProjection.totalTroops()));
        metadata.put("inTraining", Integer.toString(citizenProjection.inTraining()));
        metadata.put("wounded", Integer.toString(citizenProjection.wounded()));
        metadata.put("jobSummary", citizenProjection.jobSummary().toString());
        metadata.put("ageStageSummary", citizenProjection.ageStageSummary().toString());
        return projection(platform, "citizen-population:" + citizenProjection.scope().cacheKey(), ProjectionObjectType.CITIZEN_POPULATION, new GlobalAssetId(citizenProjection.globalAssetId()),
                "Citizen population", Optional.empty(), "ACTIVE", actions, metadata);
    }

    public FrontendProjection projectCitizenDisplayAnchor(CitizenDisplayAnchorProjection anchorProjection, GamePlatform platform, List<InteractionAction> actions) {
        java.util.LinkedHashMap<String, String> metadata = new java.util.LinkedHashMap<>(anchorProjection.metadata());
        metadata.put("scopeType", anchorProjection.scope().scopeType().name());
        metadata.put("scopeId", anchorProjection.scope().scopeId());
        metadata.put("anchorType", anchorProjection.anchorType().name());
        metadata.put("displayText", anchorProjection.displayText());
        metadata.put("count", Integer.toString(anchorProjection.count()));
        return projection(platform, anchorProjection.anchorId(), ProjectionObjectType.CITIZEN_DISPLAY_ANCHOR, new GlobalAssetId(anchorProjection.globalAssetId()),
                anchorProjection.displayText(), Optional.empty(), anchorProjection.anchorType().name(), actions, metadata);
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

    private String globalAssetForKingdom(UniversalKingdomSimulationSystem.UniversalKingdom kingdom) {
        return switch (kingdom.state()) {
            case PROTECTED -> "kingdom.protected";
            case FULL -> "kingdom.full";
            case OVERPOWERED -> "kingdom.overpowered";
            default -> "kingdom.default";
        };
    }

    private String globalAssetForClockPhase(KingdomTimePhase phase) {
        return switch (phase) {
            case DAWN -> "kingdom_clock.phase.dawn";
            case DAY -> "kingdom_clock.phase.day";
            case DUSK -> "kingdom_clock.phase.dusk";
            case NIGHT -> "kingdom_clock.phase.night";
        };
    }
}
