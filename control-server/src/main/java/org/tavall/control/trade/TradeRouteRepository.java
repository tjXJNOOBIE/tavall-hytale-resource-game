package org.tavall.control.trade;

import java.util.Optional;

public interface TradeRouteRepository {
    TradeRoute saveTradeRoute(TradeRoute tradeRoute);

    Optional<TradeRoute> findTradeRoute(TradeRouteId tradeRouteId);
}
