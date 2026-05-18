package org.tavall.control.guild;

import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public final class GuildCreationHandler implements GuildDomain {
    public GuildCreationHandler() {
    }

    public GuildCreationHandler(GuildRepository guildRepository) {
        registerGuildRepository(guildRepository);
    }

    public GuildKingdom createGuildKingdom(String name, String tag, UniversalPlayerId ownerPlayerId, Instant now) {
        GuildRepository guildRepository = getGuildRepository();
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
