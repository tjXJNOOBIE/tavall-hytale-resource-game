package org.tavall.control.petition;

import org.tavall.control.guild.GuildJobBuffCalculationHandler;
import org.tavall.control.guild.GuildJobDomain;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.IGuildDomain;

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
