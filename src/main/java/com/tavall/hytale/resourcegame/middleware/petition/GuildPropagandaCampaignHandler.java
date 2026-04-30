package com.tavall.hytale.resourcegame.middleware.petition;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobDomain;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;

public final class GuildPropagandaCampaignHandler {
    private final PetitionRepository petitionRepository;
    private final GuildJobBuffCalculationHandler guildJobBuffCalculationHandler;

    public GuildPropagandaCampaignHandler(PetitionRepository petitionRepository, GuildJobBuffCalculationHandler guildJobBuffCalculationHandler) {
        this.petitionRepository = petitionRepository;
        this.guildJobBuffCalculationHandler = guildJobBuffCalculationHandler;
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
        double reachScore = (fundingAmount / 50.0d) * (1.0d + guildJobBuffCalculationHandler.calculateJobModifier(actor, GuildJobDomain.PROPAGANDA));
        return petitionRepository.savePropagandaCampaign(new PropagandaCampaign(
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
