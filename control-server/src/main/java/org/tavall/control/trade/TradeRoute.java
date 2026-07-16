package org.tavall.control.trade;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.castle.CastleId;
import org.tavall.control.common.MetadataMaps;
import org.tavall.control.guild.GuildId;
import org.tavall.control.node.ResourceNodeId;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record TradeRoute(
        TradeRouteId routeId,
        CastleId sourceCastleId,
        Optional<CastleId> destinationCastleId,
        Optional<ResourceNodeId> destinationNodeId,
        GuildId ownerGuildId,
        Map<String, Integer> cargo,
        TradeRouteState state,
        int securityLevel,
        double travelProgress,
        GlobalAssetId globalAssetId,
        Map<String, String> metadata
) {
    public TradeRoute {
        Objects.requireNonNull(routeId, "routeId");
        Objects.requireNonNull(sourceCastleId, "sourceCastleId");
        destinationCastleId = destinationCastleId == null ? Optional.empty() : destinationCastleId;
        destinationNodeId = destinationNodeId == null ? Optional.empty() : destinationNodeId;
        Objects.requireNonNull(ownerGuildId, "ownerGuildId");
        cargo = cargo == null ? Map.of() : Map.copyOf(cargo);
        state = state == null ? TradeRouteState.PLANNED : state;
        securityLevel = Math.max(0, securityLevel);
        travelProgress = Math.max(0.0d, Math.min(1.0d, travelProgress));
        Objects.requireNonNull(globalAssetId, "globalAssetId");
        metadata = MetadataMaps.immutable(metadata);
    }

    public TradeRoute withState(TradeRouteState state) {
        return new TradeRoute(routeId, sourceCastleId, destinationCastleId, destinationNodeId, ownerGuildId, cargo, state, securityLevel, travelProgress, globalAssetId, metadata);
    }

    public TradeRoute withProgress(double travelProgress) {
        TradeRouteState updatedState = travelProgress >= 1.0d ? TradeRouteState.COMPLETED : state;
        return new TradeRoute(routeId, sourceCastleId, destinationCastleId, destinationNodeId, ownerGuildId, cargo, updatedState, securityLevel, travelProgress, globalAssetId, metadata);
    }
}
