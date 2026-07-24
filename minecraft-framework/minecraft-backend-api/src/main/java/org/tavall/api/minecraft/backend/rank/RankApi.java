package org.tavall.api.minecraft.backend.rank;

import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.permissions.UniversalPermission;
import org.tavall.api.minecraft.permissions.UniversalPermissionRole;
import org.tavall.api.minecraft.permissions.UniversalPermissionSubject;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RankApi {
    private final RankRepository repository;

    public RankApi(RankRepository repository) {
        this.repository = repository;
    }

    public RankResponse inspect(RankRequest request, Instant now) {
        if (request == null) {
            return RankResponse.unavailable("rank-unavailable", "Rank request was null.");
        }
        try {
            return switch (request.operation()) {
                case LIST -> listDefinitions(request, now);
                case INSPECT -> inspectPlayer(request, now);
                case SET_ROLE -> setRole(request, now);
            };
        } catch (RuntimeException exception) {
            return RankResponse.unavailable(request.requestId(), "Rank API error: " + safeMessage(exception));
        }
    }

    private RankResponse listDefinitions(RankRequest request, Instant now) {
        List<UniversalPermissionSubject> subjects = repository().findRankDefinitions().stream()
                .map(definition -> toDefinitionSubject(request, definition))
                .toList();
        Map<String, String> metadata = requestContext(request);
        metadata.put("definitionCount", String.valueOf(subjects.size()));
        metadata.put("requestTime", now.toString());
        return RankResponse.listed(
                request.requestId(),
                "Loaded " + subjects.size() + " rank definitions.",
                subjects,
                metadata
        );
    }

    private RankResponse inspectPlayer(RankRequest request, Instant now) {
        Optional<RankPlayerProfile> profile = resolveTargetProfile(request);
        if (profile.isEmpty()) {
            return RankResponse.unavailable(request.requestId(), targetPlayerNotFoundMessage(request));
        }
        RankPlayerProfile current = profile.get();
        Map<String, String> metadata = requestContext(request);
        metadata.put("requestTime", now.toString());
        metadata.put("platformAccountId", current.platformAccountId());
        metadata.put("displayName", current.displayName());
        metadata.put("rankName", current.rankName());
        metadata.put("powerLevel", String.valueOf(current.powerLevel()));
        metadata.put("targetResolved", "true");
        return RankResponse.inspected(
                request.requestId(),
                "Loaded rank profile for " + current.displayName() + ".",
                toPlayerSubject(request, current),
                metadata
        );
    }

    private RankResponse setRole(RankRequest request, Instant now) {
        if (request.requestedRole() == null) {
            return RankResponse.unavailable(request.requestId(), "Requested role is required for rank updates.");
        }
        RankDefinition rankDefinition = findRoleDefinition(request.requestedRole())
                .orElseGet(() -> definitionFor(request.requestedRole(), now));
        RankPlayerProfile current = resolveTargetProfile(request).orElse(null);
        if (current == null) {
            return RankResponse.unavailable(request.requestId(), targetPlayerNotFoundMessage(request));
        }
        RankPlayerProfile updated = updateProfile(current, rankDefinition, request, now);
        repository().savePlayerProfile(updated);
        List<UniversalPermissionSubject> subjects = repository().findPlayerProfiles().stream()
                .map(profile -> toPlayerSubject(request, profile))
                .toList();
        Map<String, String> metadata = requestContext(request);
        metadata.put("requestTime", now.toString());
        metadata.put("platformAccountId", updated.platformAccountId());
        metadata.put("displayName", updated.displayName());
        metadata.put("rankName", updated.rankName());
        metadata.put("powerLevel", String.valueOf(updated.powerLevel()));
        metadata.put("updatedRankDefinition", rankDefinition.rankName());
        return RankResponse.updated(
                request.requestId(),
                "Updated " + updated.displayName() + " to " + rankDefinition.rankName() + ".",
                toPlayerSubject(request, updated),
                subjects,
                metadata
        );
    }

    private Optional<RankDefinition> findRoleDefinition(UniversalPermissionRole role) {
        return repository().findRankDefinitions().stream()
                .filter(definition -> definition.rankName().equalsIgnoreCase(role.name()))
                .findFirst();
    }

    private RankDefinition definitionFor(UniversalPermissionRole role, Instant now) {
        return new RankDefinition(
                role.name(),
                role.powerLevel(),
                role.permissions().stream().map(Enum::name).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                now,
                now
        );
    }

    private RankRepository repository() {
        return repository;
    }

    private Optional<RankPlayerProfile> resolveTargetProfile(RankRequest request) {
        String targetAccountId = request.targetPlatformAccountId();
        if (targetAccountId != null && !targetAccountId.isBlank()) {
            Optional<RankPlayerProfile> byId = repository().findPlayerProfile(targetAccountId.trim());
            if (byId.isPresent()) {
                return byId;
            }
        }
        String targetDisplayName = request.targetDisplayName();
        if (targetDisplayName != null && !targetDisplayName.isBlank()) {
            return repository().findPlayerProfileByDisplayName(targetDisplayName.trim());
        }
        return Optional.empty();
    }

    private RankPlayerProfile updateProfile(RankPlayerProfile current, RankDefinition rankDefinition, RankRequest request, Instant now) {
        String displayName = resolveDisplayName(request, current);
        Map<String, String> metadata = mergeMetadata(current, request, rankDefinition);
        return new RankPlayerProfile(
                current.platformAccountId(),
                displayName,
                rankDefinition.rankName(),
                rankDefinition.powerLevel(),
                rankDefinition.permissions(),
                metadata,
                current.createdAt(),
                now
        );
    }

    private String resolveDisplayName(RankRequest request, RankPlayerProfile current) {
        if (request.targetDisplayName() != null && !request.targetDisplayName().isBlank()) {
            return request.targetDisplayName().trim();
        }
        return current.displayName();
    }

    private UniversalPermissionSubject toDefinitionSubject(RankRequest request, RankDefinition definition) {
        return new UniversalPermissionSubject(
                request.platform(),
                definition.rankName(),
                definition.rankName(),
                roleFor(definition.rankName(), definition.powerLevel()),
                permissionsFor(definition.permissions())
        );
    }

    private UniversalPermissionSubject toPlayerSubject(RankRequest request, RankPlayerProfile profile) {
        return new UniversalPermissionSubject(
                request.platform(),
                profile.platformAccountId(),
                profile.displayName(),
                roleFor(profile.rankName(), profile.powerLevel()),
                permissionsFor(profile.permissions())
        );
    }

    private UniversalPermissionRole roleFor(String rankName, int powerLevel) {
        try {
            return UniversalPermissionRole.valueOf(rankName.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            if (powerLevel >= UniversalPermissionRole.OWNER.powerLevel()) {
                return UniversalPermissionRole.OWNER;
            }
            if (powerLevel >= UniversalPermissionRole.ADMIN.powerLevel()) {
                return UniversalPermissionRole.ADMIN;
            }
            if (powerLevel >= UniversalPermissionRole.MODERATOR.powerLevel()) {
                return UniversalPermissionRole.MODERATOR;
            }
            return UniversalPermissionRole.MEMBER;
        }
    }

    private Set<UniversalPermission> permissionsFor(Set<String> permissions) {
        return permissions.stream()
                .map(permission -> {
                    try {
                        return UniversalPermission.valueOf(permission.trim().toUpperCase(java.util.Locale.ROOT));
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private Map<String, String> requestContext(RankRequest request) {
        return new LinkedHashMap<>(request.context());
    }

    private Map<String, String> mergeMetadata(RankPlayerProfile current, RankRequest request, RankDefinition rankDefinition) {
        Map<String, String> metadata = new LinkedHashMap<>(current.metadata());
        metadata.put("rankOperation", request.operation().name());
        metadata.put("sourcePlatform", request.platform().name());
        metadata.put("actorPlatformAccountId", request.actorPlatformAccountId());
        metadata.put("actorDisplayName", request.actorDisplayName());
        metadata.put("rankName", rankDefinition.rankName());
        metadata.put("powerLevel", String.valueOf(rankDefinition.powerLevel()));
        if (request.targetPlatformAccountId() != null && !request.targetPlatformAccountId().isBlank()) {
            metadata.put("targetPlatformAccountId", request.targetPlatformAccountId().trim());
        }
        if (request.targetDisplayName() != null && !request.targetDisplayName().isBlank()) {
            metadata.put("targetDisplayName", request.targetDisplayName().trim());
        }
        return metadata;
    }

    private String targetPlayerNotFoundMessage(RankRequest request) {
        if (request.targetDisplayName() != null && !request.targetDisplayName().isBlank()) {
            return "Player not found: " + request.targetDisplayName().trim();
        }
        if (request.targetPlatformAccountId() != null && !request.targetPlatformAccountId().isBlank()) {
            return "Player not found: " + request.targetPlatformAccountId().trim();
        }
        return "Player not found.";
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
