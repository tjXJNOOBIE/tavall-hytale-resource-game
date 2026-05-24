package org.tavall.api.minecraft.guild;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface IGuildContributionEventBuilder extends IDependencyInjectableInterface {
    IGuildContributionEventBuilder contributionId(UUID contributionId);

    IGuildContributionEventBuilder guildId(GuildId guildId);

    IGuildContributionEventBuilder universalPlayerId(UUID universalPlayerId);

    IGuildContributionEventBuilder sourceType(String sourceType);

    IGuildContributionEventBuilder points(long points);

    IGuildContributionEventBuilder coinContribution(long coinContribution);

    IGuildContributionEventBuilder resourceContribution(Map<GuildResourceType, Integer> resourceContribution);

    IGuildContributionEventBuilder createdAt(Instant createdAt);

    IGuildContributionEventBuilder metadata(Map<String, String> metadata);

    IGuildContributionEventBuilder copyOf(GuildContributionEvent guildContributionEvent);

    GuildContributionEvent build();
}
