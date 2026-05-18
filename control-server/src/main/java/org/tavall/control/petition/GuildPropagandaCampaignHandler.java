package org.tavall.control.petition;

import org.tavall.control.guild.GuildId;
import org.tavall.control.guild.GuildJobBuffCalculationHandler;
import org.tavall.control.guild.GuildJobDomain;
import org.tavall.control.guild.GuildMemberProfile;
import org.tavall.control.guild.GuildDomain;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;

public final class GuildPropagandaCampaignHandler implements PetitionDomain, GuildDomain {
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
