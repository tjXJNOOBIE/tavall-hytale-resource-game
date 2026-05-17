package com.tavall.resourcegame.middleware.trade;

import com.tavall.resourcegame.middleware.asset.GlobalAssetId;
import com.tavall.resourcegame.middleware.castle.CastleId;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.node.ResourceNodeId;

import java.util.Map;
import java.util.Optional;

public final class TradeRouteCreationHandler implements ITradeDomain {
    public TradeRouteCreationHandler() {
    }

    public TradeRouteCreationHandler(TradeRouteRepository tradeRouteRepository) {
        registerTradeRouteRepository(tradeRouteRepository);
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
        return getTradeRouteRepository().saveTradeRoute(tradeRoute);
    }
}
