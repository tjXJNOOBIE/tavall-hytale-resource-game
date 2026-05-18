package org.tavall.control.trade;

import org.tavall.control.asset.GlobalAssetId;
import org.tavall.control.castle.CastleId;
import org.tavall.control.guild.GuildId;
import org.tavall.control.node.ResourceNodeId;

import java.util.Map;
import java.util.Optional;

public final class TradeRouteCreationHandler implements TradeDomain {
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
