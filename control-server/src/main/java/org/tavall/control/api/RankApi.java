package org.tavall.control.api;

import org.tavall.control.IControlServerDomain;
import com.tjxjnoobie.api.dependency.IDependencyInjectableConcrete;
import org.tavall.control.runtime.ControlOperator;
import org.tavall.control.runtime.ControlOperatorRepository;
import org.tavall.control.runtime.ControlOperatorRole;
import org.tavall.control.identity.UniversalPlayerId;
import org.tavall.api.minecraft.permissions.RankOperationType;
import org.tavall.api.minecraft.permissions.RankRequest;
import org.tavall.api.minecraft.permissions.RankResponse;
import org.tavall.api.minecraft.permissions.UniversalPermissionRole;
import org.tavall.api.minecraft.permissions.UniversalPermissionSubject;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class RankApi implements IControlServerDomain, IDependencyInjectableConcrete {
    public RankResponse inspect(RankRequest request, Instant now) {
        if (request == null) {
            return RankResponse.unavailable("rank-unavailable", "Rank request was null.");
        }
        try {
            return switch (request.operation()) {
                case LIST -> listSubjects(request, now);
                case INSPECT -> inspectSubject(request, now);
                case SET_ROLE -> setRole(request, now);
            };
        } catch (RuntimeException exception) {
            return RankResponse.unavailable(request.requestId(), "Rank API error: " + safeMessage(exception));
        }
    }

    private RankResponse listSubjects(RankRequest request, Instant now) {
        List<UniversalPermissionSubject> subjects = repository().findOperators().stream()
                .map(this::toSubject)
                .toList();
        Map<String, String> metadata = requestContext(request);
        metadata.put("subjectCount", String.valueOf(subjects.size()));
        metadata.put("requestTime", now.toString());
        return RankResponse.listed(
                request.requestId(),
                "Loaded " + subjects.size() + " rank subjects.",
                subjects,
                metadata
        );
    }

    private RankResponse inspectSubject(RankRequest request, Instant now) {
        ControlOperator operator = resolveTargetOperator(request)
                .orElseGet(() -> fallbackOperator(request, now));
        Map<String, String> metadata = requestContext(request);
        metadata.put("requestTime", now.toString());
        metadata.put("operatorId", operator.operatorId().toString());
        metadata.put("enabled", String.valueOf(operator.enabled()));
        metadata.put("targetResolved", String.valueOf(resolveTargetOperator(request).isPresent()));
        return RankResponse.inspected(
                request.requestId(),
                "Loaded rank subject for " + operator.displayName() + ".",
                toSubject(operator),
                metadata
        );
    }

    private RankResponse setRole(RankRequest request, Instant now) {
        if (request.requestedRole() == null) {
            return RankResponse.unavailable(request.requestId(), "Requested role is required for rank updates.");
        }
        ControlOperator current = resolveTargetOperator(request).orElse(null);
        UUID operatorId = current != null ? current.operatorId() : resolveOperatorId(request);
        String displayName = resolveDisplayName(request, current);
        UniversalPlayerId universalPlayerId = request.targetPlatformAccountId() == null || request.targetPlatformAccountId().isBlank()
                ? null
                : UniversalPlayerId.of(resolveUuid(request.targetPlatformAccountId()));
        ControlOperator updated = new ControlOperator(
                operatorId,
                Optional.ofNullable(universalPlayerId),
                displayName,
                toControlOperatorRole(request.requestedRole()),
                true,
                current == null ? now : current.createdAt(),
                mergeMetadata(current, request)
        );
        repository().saveOperator(updated);
        List<UniversalPermissionSubject> subjects = repository().findOperators().stream()
                .map(this::toSubject)
                .toList();
        Map<String, String> metadata = requestContext(request);
        metadata.put("requestTime", now.toString());
        metadata.put("operatorId", updated.operatorId().toString());
        metadata.put("role", request.requestedRole().name());
        return RankResponse.updated(
                request.requestId(),
                "Updated " + updated.displayName() + " to " + request.requestedRole().name() + ".",
                toSubject(updated),
                subjects,
                metadata
        );
    }

    private ControlOperatorRepository repository() {
        return getControlCommandRuntime().operatorRepository();
    }

    private Optional<ControlOperator> resolveTargetOperator(RankRequest request) {
        String targetAccountId = request.targetPlatformAccountId();
        if (targetAccountId != null && !targetAccountId.isBlank()) {
            Optional<ControlOperator> byId = findByAccountId(targetAccountId);
            if (byId.isPresent()) {
                return byId;
            }
        }
        String targetDisplayName = request.targetDisplayName();
        if (targetDisplayName != null && !targetDisplayName.isBlank()) {
            return repository().findOperators().stream()
                    .filter(operator -> operator.displayName().equalsIgnoreCase(targetDisplayName))
                    .findFirst();
        }
        return Optional.empty();
    }

    private Optional<ControlOperator> findByAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return Optional.empty();
        }
        try {
            UUID operatorId = UUID.fromString(accountId);
            return repository().findOperator(operatorId);
        } catch (IllegalArgumentException ignored) {
            return repository().findOperators().stream()
                    .filter(operator -> operator.universalPlayerId().map(UniversalPlayerId::value).map(value -> value.toString().equalsIgnoreCase(accountId)).orElse(false))
                    .findFirst();
        }
    }

    private UUID resolveOperatorId(RankRequest request) {
        String accountId = request.targetPlatformAccountId();
        if (accountId != null && !accountId.isBlank()) {
            try {
                return UUID.fromString(accountId);
            } catch (IllegalArgumentException ignored) {
                return UUID.nameUUIDFromBytes(("rank:" + accountId).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
        String displayName = request.targetDisplayName();
        if (displayName != null && !displayName.isBlank()) {
            return UUID.nameUUIDFromBytes(("rank:" + displayName.toLowerCase(Locale.ROOT)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return UUID.nameUUIDFromBytes(("rank:" + request.requestId()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String resolveDisplayName(RankRequest request, ControlOperator current) {
        if (request.targetDisplayName() != null && !request.targetDisplayName().isBlank()) {
            return request.targetDisplayName();
        }
        if (current != null) {
            return current.displayName();
        }
        if (request.targetPlatformAccountId() != null && !request.targetPlatformAccountId().isBlank()) {
            return request.targetPlatformAccountId();
        }
        return "rank-subject";
    }

    private ControlOperatorRole toControlOperatorRole(UniversalPermissionRole role) {
        return switch (Objects.requireNonNull(role, "role")) {
            case MEMBER -> ControlOperatorRole.VIEWER;
            case MODERATOR -> ControlOperatorRole.OPERATOR;
            case ADMIN -> ControlOperatorRole.ADMIN;
            case OWNER -> ControlOperatorRole.OWNER;
            case SYSTEM -> ControlOperatorRole.SYSTEM;
        };
    }

    private UniversalPermissionRole toUniversalRole(ControlOperatorRole role) {
        return switch (Objects.requireNonNull(role, "role")) {
            case VIEWER -> UniversalPermissionRole.MEMBER;
            case OPERATOR -> UniversalPermissionRole.MODERATOR;
            case ADMIN -> UniversalPermissionRole.ADMIN;
            case OWNER -> UniversalPermissionRole.OWNER;
            case SYSTEM -> UniversalPermissionRole.SYSTEM;
        };
    }

    private UniversalPermissionSubject toSubject(ControlOperator operator) {
        String platformAccountId = operator.universalPlayerId()
                .map(UniversalPlayerId::value)
                .map(UUID::toString)
                .orElse(operator.operatorId().toString());
        return new UniversalPermissionSubject(
                org.tavall.api.minecraft.frontend.ResourceGameFrontendPlatform.MINECRAFT,
                platformAccountId,
                operator.displayName(),
                toUniversalRole(operator.role()),
                java.util.Set.of()
        );
    }

    private Map<String, String> requestContext(RankRequest request) {
        return new LinkedHashMap<>(request.context());
    }

    private Map<String, String> mergeMetadata(ControlOperator current, RankRequest request) {
        Map<String, String> metadata = current == null ? new LinkedHashMap<>() : new LinkedHashMap<>(current.metadata());
        metadata.put("rankOperation", request.operation().name());
        metadata.put("sourcePlatform", request.platform().name());
        metadata.put("actorPlatformAccountId", request.actorPlatformAccountId());
        metadata.put("actorDisplayName", request.actorDisplayName());
        if (request.targetPlatformAccountId() != null) {
            metadata.put("targetPlatformAccountId", request.targetPlatformAccountId());
        }
        if (request.targetDisplayName() != null) {
            metadata.put("targetDisplayName", request.targetDisplayName());
        }
        return metadata;
    }

    private String subjectNotFoundMessage(RankRequest request) {
        if (request.targetDisplayName() != null && !request.targetDisplayName().isBlank()) {
            return "Rank subject not found: " + request.targetDisplayName();
        }
        if (request.targetPlatformAccountId() != null && !request.targetPlatformAccountId().isBlank()) {
            return "Rank subject not found: " + request.targetPlatformAccountId();
        }
        return "Rank subject not found.";
    }

    private ControlOperator fallbackOperator(RankRequest request, Instant now) {
        UUID operatorId = resolveOperatorId(request);
        String displayName = resolveDisplayName(request, null);
        return new ControlOperator(
                operatorId,
                Optional.empty(),
                displayName,
                toControlOperatorRole(UniversalPermissionRole.MEMBER),
                true,
                now,
                requestContext(request)
        );
    }

    private UUID resolveUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return UUID.nameUUIDFromBytes(("rank:" + value).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
