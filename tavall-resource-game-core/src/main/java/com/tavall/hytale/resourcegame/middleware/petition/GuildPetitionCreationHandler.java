package com.tavall.hytale.resourcegame.middleware.petition;

import com.tavall.hytale.resourcegame.middleware.guild.GuildId;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.guild.GuildValidationException;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class GuildPetitionCreationHandler {
    private final PetitionRepository petitionRepository;

    public GuildPetitionCreationHandler(PetitionRepository petitionRepository) {
        this.petitionRepository = petitionRepository;
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
        return petitionRepository.savePetition(petition);
    }
}
