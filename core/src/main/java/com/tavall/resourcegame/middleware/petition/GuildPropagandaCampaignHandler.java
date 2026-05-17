package com.tavall.resourcegame.middleware.petition;

import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.resourcegame.middleware.guild.IGuildDomain;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;

public final class GuildPropagandaCampaignHandler implements IPetitionDomain, IGuildDomain {
    public GuildPropagandaCampaignHandler() {
    }

    public GuildPropagandaCampaignHandler(PetitionRepository petitionRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        registerPetitionRepository(petitionRepository);
        registerGuildJobBuffCalculationHandler(guildJobBuffCalculationHandler);
    }

    public PropagandaCampaign createPropagandaCampaign(
            GuildId guildId,
            UniversalPlayerId creatorPlayerId,
            GuildMemberProfile actor,
            String message,
            long fundingAmount,
            String targetScope,
            Instant now
    ) {
        double reachScore = (fundingAmount / 50.0d) * (1.0d + getGuildJobBuffCalculationHandler().calculateJobModifier(actor, GuildJobDomain.PROPAGANDA));
        return getPetitionRepository().savePropagandaCampaign(new PropagandaCampaign(
                PropagandaCampaignId.random(),
                guildId,
                creatorPlayerId,
                message,
                fundingAmount,
                targetScope,
                reachScore,
                now,
                Map.of()
        ));
    }
}
