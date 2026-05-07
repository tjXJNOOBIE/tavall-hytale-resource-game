package com.tavall.hytale.resourcegame.middleware.trade;

import com.tavall.hytale.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.hytale.resourcegame.middleware.castle.CastleId;
import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.node.ResourceNodeId;

import java.util.Map;
import java.util.Optional;

public final class TradeRouteCreationHandler {
    private final TradeRouteRepository tradeRouteRepository;

    public TradeRouteCreationHandler(TradeRouteRepository tradeRouteRepository) {
        this.tradeRouteRepository = tradeRouteRepository;
    }

    public TradeRoute createTradeRoute(CastleId sourceCastleId, Optional<CastleId> destinationCastleId, Optional<ResourceNodeId> destinationNodeId, GuildId ownerGuildId, Map<String, Integer> cargo) {
        TradeRoute tradeRoute = new TradeRoute(
                TradeRouteId.random(),
                sourceCastleId,
                destinationCastleId,
                destinationNodeId,
                ownerGuildId,
                cargo,
                TradeRouteState.PLANNED,
                1,
                0.0d,
                new GlobalAssetId("trade_route.active"),
                Map.of()
        );
        return tradeRouteRepository.saveTradeRoute(tradeRoute);
    }
}
