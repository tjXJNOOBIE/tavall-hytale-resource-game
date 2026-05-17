package com.tavall.resourcegame.middleware.petition;

import com.tavall.resourcegame.middleware.common.MetadataMaps;
import com.tavall.resourcegame.middleware.guild.GuildId;
import com.tavall.resourcegame.middleware.identity.UniversalPlayerId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record Petition(
        PetitionId petitionId,
        GuildId guildId,
        UniversalPlayerId creatorPlayerId,
        PetitionType petitionType,
        String message,
        int supportCount,
        long fundingAmount,
        PetitionState state,
        Instant createdAt,
        Instant expiresAt,
        Map<String, String> metadata
) {
    public Petition {
        Objects.requireNonNull(petitionId, "petitionId");
        Objects.requireNonNull(guildId, "guildId");
        Objects.requireNonNull(creatorPlayerId, "creatorPlayerId");
        Objects.requireNonNull(petitionType, "petitionType");
        message = message == null ? "" : message;
        supportCount = Math.max(0, supportCount);
        fundingAmount = Math.max(0L, fundingAmount);
        state = state == null ? PetitionState.DRAFT : state;
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        metadata = MetadataMaps.immutable(metadata);
    }

    public Petition funded(long amount) {
        long updatedFunding = fundingAmount + Math.max(0L, amount);
        PetitionState updatedState = updatedFunding > 0L ? PetitionState.FUNDED : state;
        return new Petition(petitionId, guildId, creatorPlayerId, petitionType, message, supportCount, updatedFunding, updatedState, createdAt, expiresAt, metadata);
    }
}
