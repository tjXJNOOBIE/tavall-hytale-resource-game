package com.tavall.hytale.resourcegame.middleware.guild;

import com.tavall.hytale.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public final class GuildCreationHandler {
    private final GuildRepository guildRepository;

    public GuildCreationHandler(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public GuildKingdom createGuildKingdom(String name, String tag, UniversalPlayerId ownerPlayerId, Instant now) {
        GuildId guildId = GuildId.random();
        GuildKingdom guildKingdom = new GuildKingdom(
                guildId,
                name,
                tag,
                ownerPlayerId,
                KingdomState.PROTECTED,
                now,
                now,
                GuildTreasurySnapshot.empty(),
                GuildTierPolicy.defaults(),
                Map.of(),
                Map.of()
        );
        guildRepository.saveGuild(guildKingdom);
        guildRepository.saveMember(new GuildMemberProfile(
                guildId,
                ownerPlayerId,
                GuildAuthorityTier.RULER,
                Set.copyOf(GuildTierPolicy.defaults().permissionsForTier(GuildAuthorityTier.RULER)),
                Set.of(GuildJobTitle.KING),
                now
        ));
        return guildKingdom;
    }
}
