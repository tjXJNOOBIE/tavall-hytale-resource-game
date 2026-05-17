package com.tavall.resourcegame.middleware.petition;

import com.tavall.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.resourcegame.middleware.guild.IGuildDomain;

public final class GuildPetitionReachCalculationHandler implements IPetitionDomain, IGuildDomain {
    public GuildPetitionReachCalculationHandler() {
    }

    public GuildPetitionReachCalculationHandler(GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public double calculateReach(Petition petition, GuildMemberProfile actor) {
        double baseReach = petition.supportCount() + (petition.fundingAmount() / 100.0d);
        return baseReach * (1.0d + getGuildJobBuffCalculationHandler().calculateJobModifier(actor, GuildJobDomain.PROPAGANDA));
    }
}
