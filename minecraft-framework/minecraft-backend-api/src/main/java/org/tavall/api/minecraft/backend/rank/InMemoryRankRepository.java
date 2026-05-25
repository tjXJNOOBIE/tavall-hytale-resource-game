package org.tavall.api.minecraft.backend.rank;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRankRepository implements RankRepository {
    private final Map<String, RankDefinition> definitionsByName = new ConcurrentHashMap<>();
    private final Map<String, RankPlayerProfile> profilesByAccountId = new ConcurrentHashMap<>();

    @Override
    public List<RankDefinition> findRankDefinitions() {
        return definitionsByName.values().stream()
                .sorted(Comparator.comparingInt(RankDefinition::powerLevel).thenComparing(RankDefinition::rankName))
                .toList();
    }

    @Override
    public Optional<RankDefinition> findRankDefinition(String rankName) {
        if (rankName == null || rankName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(definitionsByName.get(normalize(rankName)));
    }

    @Override
    public RankDefinition saveRankDefinition(RankDefinition definition) {
        definitionsByName.put(normalize(definition.rankName()), definition);
        return definition;
    }

    @Override
    public List<RankPlayerProfile> findPlayerProfiles() {
        return profilesByAccountId.values().stream()
                .sorted(Comparator.comparing(RankPlayerProfile::displayName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RankPlayerProfile::platformAccountId))
                .toList();
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfile(String platformAccountId) {
        if (platformAccountId == null || platformAccountId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(profilesByAccountId.get(platformAccountId));
    }

    @Override
    public Optional<RankPlayerProfile> findPlayerProfileByDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return Optional.empty();
        }
        return profilesByAccountId.values().stream()
                .filter(profile -> profile.displayName().equalsIgnoreCase(displayName))
                .findFirst();
    }

    @Override
    public RankPlayerProfile savePlayerProfile(RankPlayerProfile profile) {
        profilesByAccountId.put(profile.platformAccountId(), profile);
        return profile;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
