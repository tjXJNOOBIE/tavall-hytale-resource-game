package com.tavall.resourcegame.middleware.petition;

import com.tavall.resourcegame.middleware.guild.GuildId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPetitionRepository implements PetitionRepository {
    private final Map<PetitionId, Petition> petitionsById = new ConcurrentHashMap<>();
    private final Map<PropagandaCampaignId, PropagandaCampaign> campaignsById = new ConcurrentHashMap<>();

    @Override
    public Petition savePetition(Petition petition) {
        petitionsById.put(petition.petitionId(), petition);
        return petition;
    }

    @Override
    public Optional<Petition> findPetition(PetitionId petitionId) {
        return Optional.ofNullable(petitionsById.get(petitionId));
    }

    @Override
    public List<Petition> findPetitionsForGuild(GuildId guildId) {
        List<Petition> petitions = new ArrayList<>();
        for (Petition petition : petitionsById.values()) {
            if (petition.guildId().equals(guildId)) {
                petitions.add(petition);
            }
        }
        return List.copyOf(petitions);
    }

    @Override
    public PropagandaCampaign savePropagandaCampaign(PropagandaCampaign propagandaCampaign) {
        campaignsById.put(propagandaCampaign.campaignId(), propagandaCampaign);
        return propagandaCampaign;
    }

    @Override
    public List<PropagandaCampaign> findPropagandaCampaignsForGuild(GuildId guildId) {
        List<PropagandaCampaign> campaigns = new ArrayList<>();
        for (PropagandaCampaign campaign : campaignsById.values()) {
            if (campaign.guildId().equals(guildId)) {
                campaigns.add(campaign);
            }
        }
        return List.copyOf(campaigns);
    }
}
