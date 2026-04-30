package com.tavall.hytale.resourcegame.middleware.projection;

import com.tavall.hytale.resourcegame.middleware.common.GamePlatform;
import com.tavall.hytale.resourcegame.middleware.trade.TradeRoute;

import java.util.List;

public final class TradeRouteProjectionHandler {
    private final FrontendProjectionHandler frontendProjectionHandler;

    public TradeRouteProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        this.frontendProjectionHandler = frontendProjectionHandler;
    }

    public FrontendProjection projectTradeRoute(TradeRoute tradeRoute, GamePlatform platform, List<InteractionAction> actions) {
        return frontendProjectionHandler.projectTradeRoute(tradeRoute, platform, actions);
    }
}
