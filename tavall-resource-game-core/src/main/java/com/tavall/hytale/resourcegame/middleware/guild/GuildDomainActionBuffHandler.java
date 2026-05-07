package com.tavall.hytale.resourcegame.middleware.guild;

public final class GuildDomainActionBuffHandler {
    private final GuildJobBuffCalculationHandler guildJobBuffCalculationHandler;

    public GuildDomainActionBuffHandler(GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        this.guildJobBuffCalculationHandler = guildJobBuffCalculationHandler;
    }

    public double applyDomainActionModifier(double baseValue, GuildMemberProfile actor, GuildJobDomain domain) {
        return baseValue * (1.0d + guildJobBuffCalculationHandler.calculateJobModifier(actor, domain));
    }
}
