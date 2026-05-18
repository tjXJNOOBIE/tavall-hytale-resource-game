package org.tavall.control.economy;

import org.tavall.control.common.MetadataMaps;
import org.tavall.control.guild.GuildId;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.control.node.MiddlewareResourceType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record GuildTaxTransaction(
        UUID transactionId,
        GuildId guildId,
        UniversalPlayerId universalPlayerId,
        Optional<MiddlewareResourceType> resourceType,
        Optional<Long> coinAmount,
        Optional<Integer> resourceAmount,
        String reason,
        Instant createdAt,
        Map<String, String> metadata
) {
    public GuildTaxTransaction {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(universalPlayerId, "universalPlayerId");
        resourceType = resourceType == null ? Optional.empty() : resourceType;
        coinAmount = coinAmount == null ? Optional.empty() : coinAmount;
        resourceAmount = resourceAmount == null ? Optional.empty() : resourceAmount;
        reason = reason == null ? "" : reason;
        Objects.requireNonNull(createdAt, "createdAt");
        metadata = MetadataMaps.immutable(metadata);
    }
}
