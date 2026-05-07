package com.tavall.hytale.resourcegame.middleware.trade;

public final class TradeRouteAttackValidationHandler {
    private final TradeRouteRepository tradeRouteRepository;

    public TradeRouteAttackValidationHandler(TradeRouteRepository tradeRouteRepository) {
        this.tradeRouteRepository = tradeRouteRepository;
    }

    public TradeRoute markAttacked(TradeRouteId tradeRouteId) {
        TradeRoute route = tradeRouteRepository.findTradeRoute(tradeRouteId)
                .orElseThrow(() -> new TradeRouteValidationException("Trade route was not found."));
        return tradeRouteRepository.saveTradeRoute(route.withState(TradeRouteState.ATTACKED));
    }

    public TradeRoute markSabotaged(TradeRouteId tradeRouteId) {
        TradeRoute route = tradeRouteRepository.findTradeRoute(tradeRouteId)
                .orElseThrow(() -> new TradeRouteValidationException("Trade route was not found."));
        return tradeRouteRepository.saveTradeRoute(route.withState(TradeRouteState.SABOTAGED));
    }
}
