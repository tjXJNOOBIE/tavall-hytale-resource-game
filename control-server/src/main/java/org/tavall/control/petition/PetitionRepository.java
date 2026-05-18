package org.tavall.control.petition;

import org.tavall.control.guild.GuildId;

import java.util.List;
import java.util.Optional;

public interface PetitionRepository {
    Petition savePetition(Petition petition);

    Optional<Petition> findPetition(PetitionId petitionId);

    List<Petition> findPetitionsForGuild(GuildId guildId);

    PropagandaCampaign savePropagandaCampaign(PropagandaCampaign propagandaCampaign);

    List<PropagandaCampaign> findPropagandaCampaignsForGuild(GuildId guildId);
}
