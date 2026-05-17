package com.tavall.resourcegame.middleware.petition;

import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.resourcegame.middleware.guild.GuildValidationException;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class GuildPetitionCreationHandler implements IPetitionDomain {
    public GuildPetitionCreationHandler() {
    }

    public GuildPetitionCreationHandler(PetitionRepository petitionRepository) {
        registerPetitionRepository(petitionRepository);
    }

    public Petition createPetition(GuildId guildId, UniversalPlayerId creatorPlayerId, GuildMemberProfile memberProfile, PetitionType petitionType, String message, Instant now) {
        if (memberProfile == null || !memberProfile.guildId().equals(guildId)) {
            throw new GuildValidationException("Only guild members can create petitions.");
        }
        Petition petition = new Petition(
                PetitionId.random(),
                guildId,
                creatorPlayerId,
                petitionType,
                message,
                1,
                0L,
                PetitionState.ACTIVE,
                now,
                now.plus(Duration.ofDays(7)),
                Map.of()
        );
        return getPetitionRepository().savePetition(petition);
    }
}
