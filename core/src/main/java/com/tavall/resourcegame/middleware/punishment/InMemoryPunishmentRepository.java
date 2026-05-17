package com.tavall.resourcegame.middleware.punishment;

import com.tavall.resourcegame.shared.frontend.PunishOperationType;
import com.tavall.resourcegame.shared.frontend.PunishRecord;
import com.tavall.resourcegame.shared.frontend.PunishRequest;
import com.tavall.resourcegame.shared.frontend.PunishResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryPunishmentRepository implements PunishmentRepository {
    private final Map<String, List<PunishRecord>> punishmentsByTarget = new ConcurrentHashMap<>();

    @Override
    public PunishResponse inspect(PunishRequest request, Instant now) {
        if (request == null) {
            return PunishResponse.unavailable("punish-unavailable", "Punish request was null.");
        }
        return switch (request.operation()) {
            case INSPECT -> inspectTarget(request, now);
            case BAN -> recordBan(request, now);
            case WARN -> recordWarn(request, now);
            case UNBAN -> recordUnban(request, now);
        };
    }

    private PunishResponse inspectTarget(PunishRequest request, Instant now) {
        Optional<PunishRecord> activePunishment = findActiveBan(request);
        List<PunishRecord> punishments = history(request);
        Map<String, String> metadata = requestContext(request);
        metadata.put("requestTime", now.toString());
        metadata.put("historySize", String.valueOf(punishments.size()));
        if (activePunishment.isEmpty()) {
            return PunishResponse.inspected(
                    request.requestId(),
                    "No active punishment for " + request.targetDisplayName() + ".",
                    null,
                    punishments,
                    metadata
            );
        }
        return PunishResponse.inspected(
                request.requestId(),
                "Loaded active punishment for " + request.targetDisplayName() + ".",
                activePunishment.get(),
                punishments,
                metadata
        );
    }

    private PunishResponse recordBan(PunishRequest request, Instant now) {
        PunishRecord record = new PunishRecord(
                request.targetPlatformAccountId(),
                request.targetDisplayName(),
                PunishOperationType.BAN,
                true,
                normalizeReason(request.reason(), "No reason provided"),
                request.actorDisplayName(),
                normalizeDurationText(request.durationText()),
                now.toEpochMilli(),
                parseExpiry(request.durationText(), now),
                requestContext(request)
        );
        addRecord(request, record);
        return PunishResponse.updated(
                request.requestId(),
                "You have banned " + request.targetDisplayName() + " for " + record.durationText() + (record.reason().isBlank() ? "" : " for " + record.reason()) + ".",
                record,
                history(request),
                requestContext(request)
        );
    }

    private PunishResponse recordWarn(PunishRequest request, Instant now) {
        PunishRecord record = new PunishRecord(
                request.targetPlatformAccountId(),
                request.targetDisplayName(),
                PunishOperationType.WARN,
                false,
                normalizeReason(request.reason(), "Not provided"),
                request.actorDisplayName(),
                "",
                now.toEpochMilli(),
                null,
                requestContext(request)
        );
        addRecord(request, record);
        int warnCount = (int) history(request).stream().filter(item -> item.operation() == PunishOperationType.WARN).count();
        Map<String, String> metadata = requestContext(request);
        metadata.put("warnCount", String.valueOf(warnCount));
        return PunishResponse.updated(
                request.requestId(),
                "You have warned " + request.targetDisplayName() + (record.reason().isBlank() ? "" : " for " + record.reason()) + ". Total warns: " + warnCount,
                record,
                history(request),
                metadata
        );
    }

    private PunishResponse recordUnban(PunishRequest request, Instant now) {
        Optional<PunishRecord> activePunishment = findActiveBan(request);
        PunishRecord record = new PunishRecord(
                request.targetPlatformAccountId(),
                request.targetDisplayName(),
                PunishOperationType.UNBAN,
                false,
                "Unbanned",
                request.actorDisplayName(),
                "Permanent",
                now.toEpochMilli(),
                null,
                requestContext(request)
        );
        addRecord(request, record);
        Map<String, String> metadata = requestContext(request);
        metadata.put("hadActiveBan", String.valueOf(activePunishment.isPresent()));
        return PunishResponse.updated(
                request.requestId(),
                "You have unbanned " + request.targetDisplayName() + ".",
                record,
                history(request),
                metadata
        );
    }

    private Optional<PunishRecord> findActiveBan(PunishRequest request) {
        return history(request).stream()
                .filter(record -> record.operation() == PunishOperationType.BAN && record.active())
                .filter(record -> !isExpired(record))
                .sorted(Comparator.comparingLong(PunishRecord::createdAtEpochMillis).reversed())
                .findFirst();
    }

    private List<PunishRecord> history(PunishRequest request) {
        return punishmentsByTarget.values().stream()
                .flatMap(List::stream)
                .filter(record -> matchesTarget(record, request))
                .sorted(Comparator.comparingLong(PunishRecord::createdAtEpochMillis))
                .toList();
    }

    private void addRecord(PunishRequest request, PunishRecord record) {
        punishmentsByTarget.compute(targetKey(request), (key, existing) -> {
            List<PunishRecord> updated = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            updated.add(record);
            return List.copyOf(updated);
        });
        if (record.operation() == PunishOperationType.UNBAN) {
            punishmentsByTarget.replaceAll((key, existing) -> List.copyOf(existing.stream()
                    .map(item -> matchesTarget(item, request) && item.operation() == PunishOperationType.BAN && item.active()
                            ? deactivate(item)
                            : item)
                    .toList()));
        }
    }

    private PunishRecord deactivate(PunishRecord record) {
        return new PunishRecord(
                record.targetPlatformAccountId(),
                record.targetDisplayName(),
                record.operation(),
                false,
                record.reason(),
                record.senderDisplayName(),
                record.durationText(),
                record.createdAtEpochMillis(),
                record.expiresAtEpochMillis(),
                record.metadata()
        );
    }

    private boolean isExpired(PunishRecord record) {
        return record.expiresAtEpochMillis() != null && record.expiresAtEpochMillis() <= System.currentTimeMillis();
    }

    private String targetKey(PunishRequest request) {
        return normalize(request.targetPlatformAccountId()) + ":" + normalize(request.targetDisplayName());
    }

    private boolean matchesTarget(PunishRecord record, PunishRequest request) {
        return normalize(record.targetPlatformAccountId()).equals(normalize(request.targetPlatformAccountId()))
                || normalize(record.targetDisplayName()).equals(normalize(request.targetDisplayName()));
    }

    private Map<String, String> requestContext(PunishRequest request) {
        return new LinkedHashMap<>(request.context());
    }

    private Long parseExpiry(String durationText, Instant now) {
        String normalized = normalize(durationText);
        if (normalized.isBlank() || normalized.startsWith("p") || normalized.startsWith("perm")) {
            return null;
        }
        long amount;
        long millisPerUnit;
        if (normalized.endsWith("mo")) {
            amount = parseLeadingLong(normalized.substring(0, normalized.length() - 2), 1L);
            millisPerUnit = 30L * 24L * 60L * 60L * 1000L;
        } else if (normalized.endsWith("yr")) {
            amount = parseLeadingLong(normalized.substring(0, normalized.length() - 2), 1L);
            millisPerUnit = 365L * 24L * 60L * 60L * 1000L;
        } else {
            String unit = normalized.substring(normalized.length() - 1);
            amount = parseLeadingLong(normalized.substring(0, normalized.length() - 1), 1L);
            millisPerUnit = switch (unit) {
                case "m" -> 60L * 1000L;
                case "h" -> 60L * 60L * 1000L;
                case "d" -> 24L * 60L * 60L * 1000L;
                case "w" -> 7L * 24L * 60L * 60L * 1000L;
                default -> 0L;
            };
        }
        if (millisPerUnit <= 0L) {
            return null;
        }
        return now.toEpochMilli() + (amount * millisPerUnit);
    }

    private long parseLeadingLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String normalizeReason(String reason, String fallback) {
        if (reason == null || reason.isBlank()) {
            return fallback;
        }
        return reason;
    }

    private String normalizeDurationText(String durationText) {
        if (durationText == null || durationText.isBlank()) {
            return "Permanent";
        }
        String normalized = durationText.trim();
        if (normalized.equalsIgnoreCase("p") || normalized.equalsIgnoreCase("permanent")) {
            return "Permanent";
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
