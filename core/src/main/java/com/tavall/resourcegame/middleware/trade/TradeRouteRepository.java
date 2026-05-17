package com.tavall.resourcegame.middleware.trade;

import java.util.Optional;

public interface TradeRouteRepository {
    TradeRoute saveTradeRoute(TradeRoute tradeRoute);

    Optional<TradeRoute> findTradeRoute(TradeRouteId tradeRouteId);
}
