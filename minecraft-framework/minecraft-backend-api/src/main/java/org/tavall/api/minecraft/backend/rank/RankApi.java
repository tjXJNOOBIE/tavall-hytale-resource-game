package org.tavall.api.minecraft.backend.rank;

import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.permissions.RankSubject;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                case SET -> setRank(request, now);
                case REMOVE -> removeRank(request, now);
            };
        } catch (RuntimeException exception) {
            return RankResponse.unavailable(request.requestId(), "Rank API error: " + safeMessage(exception));
        }
    }

    private RankResponse listDefinitions(RankRequest request, Instant now) {
        List<RankSubject> subjects = repository().findRankDefinitions().stream()
                .map(this::toDefinitionSubject)
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
                toPlayerSubject(current),
                metadata
        );
    }

    private RankResponse setRank(RankRequest request, Instant now) {
        if (request.requestedRankName() == null) {
            return RankResponse.unavailable(request.requestId(), "Requested rank is required for rank updates.");
        }
        RankDefinition rankDefinition = repository().findRankDefinition(request.requestedRankName())
                .orElse(null);
        if (rankDefinition == null) {
            return RankResponse.unavailable(request.requestId(), rankNotFoundMessage(request.requestedRankName()));
        }
        RankPlayerProfile current = resolveTargetProfile(request).orElse(null);
        if (current == null) {
            return RankResponse.unavailable(request.requestId(), targetPlayerNotFoundMessage(request));
        }
        RankPlayerProfile updated = updateProfile(current, rankDefinition, request, now);
        repository().savePlayerProfile(updated);
        List<RankSubject> subjects = repository().findPlayerProfiles().stream()
                .map(this::toPlayerSubject)
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
                toPlayerSubject(updated),
                subjects,
                metadata
        );
    }

    private RankResponse removeRank(RankRequest request, Instant now) {
        RankPlayerProfile current = resolveTargetProfile(request).orElse(null);
        if (current == null) {
            return RankResponse.unavailable(request.requestId(), targetPlayerNotFoundMessage(request));
        }
        String fallbackRankName = request.fallbackRankName() == null ? "Member" : request.fallbackRankName();
        RankDefinition fallbackDefinition = repository().findRankDefinition(fallbackRankName).orElse(null);
        if (fallbackDefinition == null) {
            return RankResponse.unavailable(request.requestId(), rankNotFoundMessage(fallbackRankName));
        }
        RankPlayerProfile updated = updateProfile(current, fallbackDefinition, request, now);
        repository().savePlayerProfile(updated);
        List<RankSubject> subjects = repository().findPlayerProfiles().stream()
                .map(this::toPlayerSubject)
                .toList();
        Map<String, String> metadata = requestContext(request);
        metadata.put("requestTime", now.toString());
        metadata.put("platformAccountId", updated.platformAccountId());
        metadata.put("displayName", updated.displayName());
        metadata.put("rankName", updated.rankName());
        metadata.put("powerLevel", String.valueOf(updated.powerLevel()));
        metadata.put("fallbackRankName", fallbackDefinition.rankName());
        return RankResponse.updated(
                request.requestId(),
                "Reverted " + updated.displayName() + " to " + fallbackDefinition.rankName() + ".",
                toPlayerSubject(updated),
                subjects,
                metadata
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

    private RankSubject toDefinitionSubject(RankDefinition definition) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("definition", "true");
        metadata.put("createdAt", definition.createdAt().toString());
        metadata.put("updatedAt", definition.updatedAt().toString());
        return new RankSubject(
                definition.rankName(),
                definition.rankName(),
                definition.rankName(),
                definition.powerLevel(),
                definition.permissions(),
                metadata
        );
    }

    private RankSubject toPlayerSubject(RankPlayerProfile profile) {
        return new RankSubject(
                profile.platformAccountId(),
                profile.displayName(),
                profile.rankName(),
                profile.powerLevel(),
                profile.permissions(),
                profile.metadata()
        );
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

    private String rankNotFoundMessage(String rankName) {
        return "Rank not found: " + rankName;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
