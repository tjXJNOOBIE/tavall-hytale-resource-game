package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

public final class GuildAuthorityTierHandler {
    private final GuildRepository guildRepository;

    public GuildAuthorityTierHandler(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public GuildMemberProfile setAuthorityTier(GuildId guildId, UniversalPlayerId universalPlayerId, GuildAuthorityTier authorityTier) {
        GuildMemberProfile member = guildRepository.findMember(guildId, universalPlayerId)
                .orElseThrow(() -> new GuildValidationException("Guild member was not found."));
        return guildRepository.saveMember(member.withAuthorityTier(authorityTier));
    }
}
