package com.tavall.hytale.resourcegame.middleware.trade;

import com.tavall.hytale.resourcegame.middleware.castle.CastleId;
import com.tavall.hytale.resourcegame.middleware.guild.GuildCreationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobAssignmentHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobTitle;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMembershipHandler;
import com.tavall.hytale.resourcegame.middleware.guild.InMemoryGuildRepository;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TradeRouteMiddlewareIntegrationTest {
    @Test
    void tradeRouteProgressAttackAndSabotageWorkThroughHandlers() {
        InMemoryTradeRouteRepository routeRepository = new InMemoryTradeRouteRepository();
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        Instant now = Instant.parse("2026-04-30T13:20:00Z");
        GuildKingdom guild = new GuildCreationHandler(guildRepository).createGuildKingdom("Trade", "TRD", UniversalPlayerId.random(), now);
        GuildMemberProfile member = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guild.guildId(), UniversalPlayerId.random(), now);
        GuildMemberProfile quartermaster = new GuildJobAssignmentHandler(guildRepository).assignJobTitle(guild.guildId(), member.universalPlayerId(), GuildJobTitle.QUARTERMASTER, now);
        TradeRoute route = new TradeRouteCreationHandler(routeRepository)
                .createTradeRoute(CastleId.random(), Optional.of(CastleId.random()), Optional.empty(), guild.guildId(), Map.of("wood", 50));

        TradeRoute progressed = new TradeRouteProgressTickHandler(routeRepository, new GuildJobBuffCalculationHandler())
                .progressRoute(route.routeId(), 0.50d, quartermaster);

        assertEquals(0.55d, progressed.travelProgress(), 0.0001d);
        assertEquals(TradeRouteState.ATTACKED, new TradeRouteAttackValidationHandler(routeRepository).markAttacked(route.routeId()).state());
        assertEquals(TradeRouteState.SABOTAGED, new TradeRouteAttackValidationHandler(routeRepository).markSabotaged(route.routeId()).state());
        assertEquals("trade_route.active", route.globalAssetId().value());
    }
}
