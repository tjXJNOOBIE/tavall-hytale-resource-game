package org.tavall.control.projection;

import org.tavall.control.common.GamePlatform;
import org.tavall.control.trade.TradeRoute;

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
