package org.tavall.control.petition;

import org.tavall.control.common.MetadataMaps;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record PropagandaCampaign(
        PropagandaCampaignId campaignId,
        GuildId guildId,
        UniversalPlayerId creatorPlayerId,
        String message,
        long fundingAmount,
        String targetScope,
        double reachScore,
        Instant createdAt,
        Map<String, String> metadata
) {
    public PropagandaCampaign {
        Objects.requireNonNull(campaignId, "campaignId");
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(creatorPlayerId, "creatorPlayerId");
        message = message == null ? "" : message;
        targetScope = targetScope == null || targetScope.isBlank() ? "guild" : targetScope;
        reachScore = Math.max(0.0d, reachScore);
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
