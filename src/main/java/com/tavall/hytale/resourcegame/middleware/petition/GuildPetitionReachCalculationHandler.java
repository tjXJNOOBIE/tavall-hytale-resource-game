package com.tavall.hytale.resourcegame.middleware.petition;

import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;

public final class GuildPetitionReachCalculationHandler {
    private final GuildJobBuffCalculationHandler guildJobBuffCalculationHandler;

    public GuildPetitionReachCalculationHandler(GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        this.guildJobBuffCalculationHandler = guildJobBuffCalculationHandler;
    }

    public double calculateReach(Petition petition, GuildMemberProfile actor) {
        double baseReach = petition.supportCount() + (petition.fundingAmount() / 100.0d);
        return baseReach * (1.0d + guildJobBuffCalculationHandler.calculateJobModifier(actor, GuildJobDomain.PROPAGANDA));
    }
}
