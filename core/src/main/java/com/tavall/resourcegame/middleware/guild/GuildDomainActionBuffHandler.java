package com.tavall.resourcegame.middleware.guild;

public final class GuildDomainActionBuffHandler implements IGuildDomain {
    public GuildDomainActionBuffHandler() {
    }

    public GuildDomainActionBuffHandler(GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public double applyDomainActionModifier(double baseValue, GuildMemberProfile actor, GuildJobDomain domain) {
        return baseValue * (1.0d + getGuildJobBuffCalculationHandler().calculateJobModifier(actor, domain));
    }
}
