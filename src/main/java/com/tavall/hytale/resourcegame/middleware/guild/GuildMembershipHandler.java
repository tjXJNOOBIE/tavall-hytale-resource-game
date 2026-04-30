package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Set;

public final class GuildMembershipHandler {
    private final GuildRepository guildRepository;

    public GuildMembershipHandler(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public GuildMemberProfile addPlayerToGuild(GuildId guildId, UniversalPlayerId universalPlayerId, Instant now) {
        return guildRepository.saveMember(new GuildMemberProfile(
                guildId,
                universalPlayerId,
                GuildAuthorityTier.CITIZEN,
                Set.of(),
                Set.of(GuildJobTitle.MEMBER),
                now
        ));
    }
}
