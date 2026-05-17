package com.tavall.resourcegame.middleware.projection;

import com.tavall.resourcegame.middleware.common.GamePlatform;
import com.tavall.resourcegame.middleware.trade.TradeRoute;

import java.util.List;

public final class TradeRouteProjectionHandler implements IProjectionDomain {
    public TradeRouteProjectionHandler() {
    }

    public TradeRouteProjectionHandler(FrontendProjectionHandler frontendProjectionHandler) {
        registerFrontendProjectionHandler(frontendProjectionHandler);
    }

    public FrontendProjection projectTradeRoute(TradeRoute tradeRoute, GamePlatform platform, List<InteractionAction> actions) {
        return getFrontendProjectionHandler().projectTradeRoute(tradeRoute, platform, actions);
    }
}
