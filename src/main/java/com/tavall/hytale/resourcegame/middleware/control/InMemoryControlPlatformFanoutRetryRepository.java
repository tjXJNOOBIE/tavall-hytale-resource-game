package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryControlPlatformFanoutRetryRepository implements ControlPlatformFanoutRetryRepository {
    private final Map<UUID, ControlPlatformFanoutRetryRecord> retryRecordsById = new ConcurrentHashMap<>();

    @Override
    public ControlPlatformFanoutRetryRecord saveRetryRecord(ControlPlatformFanoutRetryRecord retryRecord) {
        retryRecordsById.put(retryRecord.retryId(), retryRecord);
        return retryRecord;
    }

    @Override
    public Optional<ControlPlatformFanoutRetryRecord> findRetryRecord(UUID retryId) {
        return Optional.ofNullable(retryRecordsById.get(retryId));
    }

    @Override
    public List<ControlPlatformFanoutRetryRecord> findRetriesForCommand(ControlCommandId commandId) {
        return retryRecordsById.values().stream()
                .filter(retryRecord -> retryRecord.commandId().equals(commandId))
                .sorted(Comparator.comparing(ControlPlatformFanoutRetryRecord::createdAt))
                .toList();
    }

    @Override
    public List<ControlPlatformFanoutRetryRecord> findDueRetries(Instant now, int limit) {
        return retryRecordsById.values().stream()
                .filter(retryRecord -> retryRecord.state() == ControlPlatformFanoutRetryState.PENDING || retryRecord.state() == ControlPlatformFanoutRetryState.RETRYING)
                .filter(retryRecord -> !retryRecord.nextAttemptAt().isAfter(now))
                .sorted(Comparator.comparing(ControlPlatformFanoutRetryRecord::nextAttemptAt))
                .limit(Math.max(0, limit))
                .toList();
    }
}
