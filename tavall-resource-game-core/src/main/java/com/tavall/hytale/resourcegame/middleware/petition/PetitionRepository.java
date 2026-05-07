package com.tavall.hytale.resourcegame.middleware.petition;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;

import java.util.List;
import java.util.Optional;

public interface PetitionRepository {
    Petition savePetition(Petition petition);

    Optional<Petition> findPetition(PetitionId petitionId);

    List<Petition> findPetitionsForGuild(GuildId guildId);

    PropagandaCampaign savePropagandaCampaign(PropagandaCampaign propagandaCampaign);

    List<PropagandaCampaign> findPropagandaCampaignsForGuild(GuildId guildId);
}
