package org.tavall.api.minecraft.backend.rank;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryRankRepository implements RankAccess {
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
    public Optional<RankPlayerProfile> findPlayerProfileByUsername(String username) {
        return findPlayerProfileByDisplayName(username);
    }

    @Override
    public RankPlayerProfile savePlayerProfile(RankPlayerProfile profile) {
        profilesByAccountId.put(profile.platformAccountId(), profile);
        return profile;
    }

    @Override
    public void createProfile(java.util.UUID playerId, String playerName) {
        throw new UnsupportedOperationException("In-memory rank repository does not create database-backed profiles.");
    }

    @Override
    public boolean playerExistsByUsername(String username) {
        return findPlayerProfileByDisplayName(username).isPresent();
    }

    @Override
    public void setRankFromUsername(String username, String rank) {
        findPlayerProfileByDisplayName(username).ifPresent(profile ->
                savePlayerProfile(new RankPlayerProfile(
                        profile.platformAccountId(),
                        profile.displayName(),
                        rank,
                        profile.powerLevel(),
                        profile.permissions(),
                        profile.metadata(),
                        profile.createdAt(),
                        profile.updatedAt()
                )));
    }

    @Override
    public void setRank(java.util.UUID uuid, String rank) {
        findPlayerProfile(uuid.toString()).ifPresent(profile ->
                savePlayerProfile(new RankPlayerProfile(
                        profile.platformAccountId(),
                        profile.displayName(),
                        rank,
                        profile.powerLevel(),
                        profile.permissions(),
                        profile.metadata(),
                        profile.createdAt(),
                        profile.updatedAt()
                )));
    }

    @Override
    public void revokeRank(java.util.UUID uuid, String fallbackRankName) {
        String fallback = fallbackRankName == null || fallbackRankName.isBlank() ? "Member" : fallbackRankName;
        setRank(uuid, fallback);
    }

    @Override
    public String getRank(java.util.UUID uuid) {
        return findPlayerProfile(uuid.toString()).map(RankPlayerProfile::rankName).orElse("Couldn't get rank");
    }

    @Override
    public int getPowerLevel(java.util.UUID uuid) {
        return findPlayerProfile(uuid.toString()).map(RankPlayerProfile::powerLevel).orElse(1);
    }

    @Override
    public java.util.Set<String> getPermissions(java.util.UUID uuid) {
        return findPlayerProfile(uuid.toString()).map(RankPlayerProfile::permissions).orElse(Set.of());
    }

    @Override
    public void setPermissions(java.util.UUID uuid, java.util.Set<String> permissions) {
        findPlayerProfile(uuid.toString()).ifPresent(profile ->
                savePlayerProfile(new RankPlayerProfile(
                        profile.platformAccountId(),
                        profile.displayName(),
                        profile.rankName(),
                        profile.powerLevel(),
                        permissions,
                        profile.metadata(),
                        profile.createdAt(),
                        profile.updatedAt()
                )));
    }

    @Override
    public boolean hasPermission(java.util.UUID uuid, String permission) {
        return getPermissions(uuid).contains(permission);
    }

    @Override
    public boolean rankExists(String rankName) {
        return findRankDefinition(rankName).isPresent();
    }

    @Override
    public List<String> getRanks() {
        return findRankDefinitions().stream().map(RankDefinition::rankName).toList();
    }

    @Override
    public String getAllRanks() {
        List<String> ranks = getRanks();
        return ranks.isEmpty() ? "" : ranks.get(ranks.size() - 1);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
