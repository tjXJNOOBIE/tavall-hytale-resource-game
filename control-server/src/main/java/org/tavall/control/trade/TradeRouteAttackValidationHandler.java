package org.tavall.control.trade;

public final class TradeRouteAttackValidationHandler implements ITradeDomain {
    public TradeRouteAttackValidationHandler() {
    }

    public TradeRouteAttackValidationHandler(TradeRouteRepository tradeRouteRepository) {
        registerTradeRouteRepository(tradeRouteRepository);
    }

    public TradeRoute markAttacked(TradeRouteId tradeRouteId) {
        TradeRoute route = getTradeRouteRepository().findTradeRoute(tradeRouteId)
                .orElseThrow(() -> new TradeRouteValidationException("Trade route was not found."));
        return getTradeRouteRepository().saveTradeRoute(route.withState(TradeRouteState.ATTACKED));
    }

    public TradeRoute markSabotaged(TradeRouteId tradeRouteId) {
        TradeRoute route = getTradeRouteRepository().findTradeRoute(tradeRouteId)
                .orElseThrow(() -> new TradeRouteValidationException("Trade route was not found."));
        return getTradeRouteRepository().saveTradeRoute(route.withState(TradeRouteState.SABOTAGED));
    }
}
