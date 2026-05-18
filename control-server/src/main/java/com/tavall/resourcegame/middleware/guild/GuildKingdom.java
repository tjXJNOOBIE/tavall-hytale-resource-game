package org.tavall.control.guild;

import org.tavall.control.common.MetadataMaps;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record GuildKingdom(
        GuildId guildId,
        String name,
        String tag,
        UniversalPlayerId ownerPlayerId,
        KingdomState state,
        Instant createdAt,
        Instant updatedAt,
        GuildTreasurySnapshot treasury,
        GuildTierPolicy tierPolicy,
        Map<String, String> activeBuffs,
        Map<String, String> metadata
) {
    public GuildKingdom {
        Objects.requireNonNull(guildId, "guildId");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("guild name is required.");
        }
        tag = tag == null ? "" : tag.toUpperCase();
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        state = state == null ? KingdomState.PROTECTED : state;
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        treasury = treasury == null ? GuildTreasurySnapshot.empty() : treasury;
        tierPolicy = tierPolicy == null ? GuildTierPolicy.defaults() : tierPolicy;
        activeBuffs = MetadataMaps.immutable(activeBuffs);
        metadata = MetadataMaps.immutable(metadata);
    }

    public GuildKingdom withState(KingdomState state, Instant now) {
        return new GuildKingdom(guildId, name, tag, ownerPlayerId, state, createdAt, now, treasury, tierPolicy, activeBuffs, metadata);
    }
}
