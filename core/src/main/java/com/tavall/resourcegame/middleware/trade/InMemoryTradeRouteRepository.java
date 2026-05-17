package com.tavall.resourcegame.middleware.trade;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryTradeRouteRepository implements TradeRouteRepository {
    private final Map<TradeRouteId, TradeRoute> routesById = new ConcurrentHashMap<>();

    @Override
    public TradeRoute saveTradeRoute(TradeRoute tradeRoute) {
        routesById.put(tradeRoute.routeId(), tradeRoute);
        return tradeRoute;
    }

    @Override
    public Optional<TradeRoute> findTradeRoute(TradeRouteId tradeRouteId) {
        return Optional.ofNullable(routesById.get(tradeRouteId));
    }
}
