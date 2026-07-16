package org.tavall.control.trade;

import org.tavall.control.guild.GuildJobBuffCalculationHandler;
import org.tavall.control.guild.GuildJobDomain;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.GuildDomain;

public final class TradeRouteProgressTickHandler implements TradeDomain, GuildDomain {
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
