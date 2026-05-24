package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableConcrete;
import org.tavall.dependency.annotations.DelegatesToInterface;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@DelegatesToInterface(getLinkedInterface = IGuildContributionEventBuilder.class)
public final class GuildContributionEventBuilder implements IGuildContributionEventBuilder, IDependencyInjectableConcrete {
    private final UUID contributionId;
    private final GuildId guildId;
    private final UUID universalPlayerId;
    private final String sourceType;
    private final long points;
    private final long coinContribution;
    private final Map<GuildResourceType, Integer> resourceContribution;
    private final Instant createdAt;
    private final Map<String, String> metadata;

    public GuildContributionEventBuilder() {
        this(UUID.randomUUID(), null, null, "guild_contribution", 0L, 0L, Map.of(), Instant.now(), Map.of());
    }

    private GuildContributionEventBuilder(
            UUID contributionId,
            GuildId guildId,
            UUID universalPlayerId,
            String sourceType,
            long points,
            long coinContribution,
            Map<GuildResourceType, Integer> resourceContribution,
            Instant createdAt,
            Map<String, String> metadata
    ) {
        this.contributionId = contributionId;
        this.guildId = guildId;
        this.universalPlayerId = universalPlayerId;
        this.sourceType = sourceType;
        this.points = points;
        this.coinContribution = coinContribution;
        this.resourceContribution = resourceContribution == null ? Map.of() : Map.copyOf(resourceContribution);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    @Override
    public IGuildContributionEventBuilder contributionId(UUID contributionId) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder guildId(GuildId guildId) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder universalPlayerId(UUID universalPlayerId) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder sourceType(String sourceType) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder points(long points) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder coinContribution(long coinContribution) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder resourceContribution(Map<GuildResourceType, Integer> resourceContribution) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder createdAt(Instant createdAt) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder metadata(Map<String, String> metadata) {
        return new GuildContributionEventBuilder(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }

    @Override
    public IGuildContributionEventBuilder copyOf(GuildContributionEvent guildContributionEvent) {
        Objects.requireNonNull(guildContributionEvent, "guildContributionEvent");
        return new GuildContributionEventBuilder(
                guildContributionEvent.contributionId(),
                guildContributionEvent.guildId(),
                guildContributionEvent.universalPlayerId(),
                guildContributionEvent.sourceType(),
                guildContributionEvent.points(),
                guildContributionEvent.coinContribution(),
                guildContributionEvent.resourceContribution(),
                guildContributionEvent.createdAt(),
                guildContributionEvent.metadata()
        );
    }

    @Override
    public GuildContributionEvent build() {
        return new GuildContributionEvent(contributionId, guildId, universalPlayerId, sourceType, points, coinContribution, resourceContribution, createdAt, metadata);
    }
}
