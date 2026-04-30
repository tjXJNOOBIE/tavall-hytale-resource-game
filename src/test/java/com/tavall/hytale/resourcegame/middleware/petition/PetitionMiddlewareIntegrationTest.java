package com.tavall.hytale.resourcegame.middleware.petition;

import com.tavall.hytale.resourcegame.middleware.guild.GuildCreationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobAssignmentHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobBuffCalculationHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildJobTitle;
import com.tavall.hytale.resourcegame.middleware.guild.GuildKingdom;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMemberProfile;
import com.tavall.hytale.resourcegame.middleware.guild.GuildMembershipHandler;
import com.tavall.hytale.resourcegame.middleware.guild.GuildValidationException;
import com.tavall.hytale.resourcegame.middleware.guild.InMemoryGuildRepository;
import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class PetitionMiddlewareIntegrationTest {
    @Test
    void petitionsRequireMembershipCanBeFundedAndReceivePropagandistReach() {
        InMemoryGuildRepository guildRepository = new InMemoryGuildRepository();
        InMemoryPetitionRepository petitionRepository = new InMemoryPetitionRepository();
        GuildPetitionCreationHandler creationHandler = new GuildPetitionCreationHandler(petitionRepository);
        Instant now = Instant.parse("2026-04-30T13:10:00Z");
        UniversalPlayerId owner = UniversalPlayerId.random();
        GuildKingdom guild = new GuildCreationHandler(guildRepository).createGuildKingdom("Petitions", "PET", owner, now);
        GuildMemberProfile member = new GuildMembershipHandler(guildRepository).addPlayerToGuild(guild.guildId(), UniversalPlayerId.random(), now);

        assertThrows(GuildValidationException.class, () -> creationHandler.createPetition(guild.guildId(), UniversalPlayerId.random(), null, PetitionType.LOWER_TAXES, "Lower taxes", now));

        Petition petition = creationHandler.createPetition(guild.guildId(), member.universalPlayerId(), member, PetitionType.LOWER_TAXES, "Lower taxes", now);
        Petition funded = new GuildPetitionFundingHandler(petitionRepository).fundPetition(petition.petitionId(), 500L);

        assertEquals(PetitionState.FUNDED, funded.state());
        assertEquals(500L, funded.fundingAmount());

        GuildMemberProfile propagandist = new GuildJobAssignmentHandler(guildRepository).assignJobTitle(guild.guildId(), member.universalPlayerId(), GuildJobTitle.PROPAGANDIST, now);
        double reach = new GuildPetitionReachCalculationHandler(new GuildJobBuffCalculationHandler()).calculateReach(funded, propagandist);
        assertEquals(6.9d, reach, 0.0001d);
    }

    @Test
    void overthrowDoesNotResolveInstantlyWithoutExplicitPolicy() {
        Instant now = Instant.parse("2026-04-30T13:15:00Z");
        Petition petition = new Petition(
                PetitionId.random(),
                com.tavall.hytale.resourcegame.middleware.guild.GuildId.random(),
                UniversalPlayerId.random(),
                PetitionType.REMOVE_LEADER,
                "Remove leader",
                100,
                5_000L,
                PetitionState.VIRAL,
                now,
                now.plusSeconds(3600),
                java.util.Map.of()
        );

        GuildOverthrowFlowHandler overthrowFlowHandler = new GuildOverthrowFlowHandler();
        assertFalse(overthrowFlowHandler.canResolveOverthrowImmediately(petition, false));
        assertTrue(overthrowFlowHandler.canResolveOverthrowImmediately(petition, true));
    }
}
