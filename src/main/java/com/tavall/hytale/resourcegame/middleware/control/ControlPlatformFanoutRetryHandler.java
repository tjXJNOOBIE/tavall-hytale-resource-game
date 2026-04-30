package com.tavall.hytale.resourcegame.middleware.control;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ControlPlatformFanoutRetryHandler {
    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMinutes(1L);

    private final ControlPlatformFanoutRetryRepository retryRepository;

    public ControlPlatformFanoutRetryHandler(ControlPlatformFanoutRetryRepository retryRepository) {
        this.retryRepository = retryRepository;
    }

    public List<ControlPlatformFanoutRetryRecord> recordFailedFanout(ControlCommand command, List<String> changedObjectIds, List<PlatformCommandResult> platformResults, Instant now) {
        return platformResults.stream()
                .filter(platformResult -> !platformResult.success())
                .map(platformResult -> retryRepository.saveRetryRecord(new ControlPlatformFanoutRetryRecord(
                        UUID.randomUUID(),
                        command.commandId(),
                        command.commandType(),
                        platformResult.platform(),
                        changedObjectIds,
                        0,
                        DEFAULT_MAX_ATTEMPTS,
                        now.plus(DEFAULT_RETRY_DELAY),
                        java.util.Optional.empty(),
                        ControlPlatformFanoutRetryState.PENDING,
                        platformResult.message(),
                        now,
                        now,
                        Map.of("pendingReason", platformResult.metadata().getOrDefault("pending", "false"))
                )))
                .toList();
    }

    public List<ControlPlatformFanoutRetryRecord> dueRetries(Instant now, int limit) {
        return retryRepository.findDueRetries(now, limit);
    }

    public ControlPlatformFanoutRetryRecord markRetryAttempted(UUID retryId, boolean success, Instant now, String message) {
        ControlPlatformFanoutRetryRecord retryRecord = retryRepository.findRetryRecord(retryId)
                .orElseThrow(() -> new ControlCommandValidationException("Fanout retry record was not found."));
        ControlPlatformFanoutRetryRecord updated = success
                ? retryRecord.withState(ControlPlatformFanoutRetryState.SUCCEEDED, now, message)
                : retryRecord.withAttemptScheduled(now.plus(DEFAULT_RETRY_DELAY), now, message);
        return retryRepository.saveRetryRecord(updated);
    }
}
