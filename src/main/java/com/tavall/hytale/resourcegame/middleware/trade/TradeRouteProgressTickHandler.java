package com.tavall.hytale.resourcegame.middleware.trade;

import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;

public final class TradeRouteProgressTickHandler {
    private final TradeRouteRepository tradeRouteRepository;
    private final GuildJobBuffCalculationHandler guildJobBuffCalculationHandler;

    public TradeRouteProgressTickHandler(TradeRouteRepository tradeRouteRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        this.tradeRouteRepository = tradeRouteRepository;
        this.guildJobBuffCalculationHandler = guildJobBuffCalculationHandler;
    }

    public TradeRoute progressRoute(TradeRouteId tradeRouteId, double baseProgressDelta, GuildMemberProfile actor) {
        TradeRoute route = tradeRouteRepository.findTradeRoute(tradeRouteId)
                .orElseThrow(() -> new TradeRouteValidationException("Trade route was not found."));
        double modifier = guildJobBuffCalculationHandler.calculateJobModifier(actor, GuildJobDomain.LOGISTICS);
        return tradeRouteRepository.saveTradeRoute(route.withProgress(route.travelProgress() + (baseProgressDelta * (1.0d + modifier))));
    }
}
