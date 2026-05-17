package com.tavall.resourcegame.middleware.trade;

import com.tavall.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.resourcegame.middleware.guild.IGuildDomain;

public final class TradeRouteProgressTickHandler implements ITradeDomain, IGuildDomain {
    public TradeRouteProgressTickHandler() {
    }

    public TradeRouteProgressTickHandler(TradeRouteRepository tradeRouteRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerTradeRouteRepository(tradeRouteRepository);
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public TradeRoute progressRoute(TradeRouteId tradeRouteId, double baseProgressDelta, GuildMemberProfile actor) {
        TradeRoute route = getTradeRouteRepository().findTradeRoute(tradeRouteId)
                .orElseThrow(() -> new TradeRouteValidationException("Trade route was not found."));
        double modifier = getGuildJobBuffCalculationHandler().calculateJobModifier(actor, GuildJobDomain.LOGISTICS);
        return getTradeRouteRepository().saveTradeRoute(route.withProgress(route.travelProgress() + (baseProgressDelta * (1.0d + modifier))));
    }
}
