package com.tavall.resourcegame.middleware.control;

import com.tavall.resourcegame.dependency.IDependencyInjectableConcrete;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ControlPlatformFanoutRetryHandler implements IControlCommandDomain, IDependencyInjectableConcrete {
    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMinutes(1L);

    public List<ControlPlatformFanoutRetryRecord> recordFailedFanout(ControlCommand command, List<String> changedObjectIds, List<PlatformCommandResult> platformResults, Instant now) {
        return platformResults.stream()
                .filter(platformResult -> !platformResult.success())
                .map(platformResult -> getControlPlatformFanoutRetryRepository().saveRetryRecord(new ControlPlatformFanoutRetryRecord(
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
        return getControlPlatformFanoutRetryRepository().findDueRetries(now, limit);
    }

    public ControlPlatformFanoutRetryRecord markRetryAttempted(UUID retryId, boolean success, Instant now, String message) {
        ControlPlatformFanoutRetryRecord retryRecord = getControlPlatformFanoutRetryRepository().findRetryRecord(retryId)
                .orElseThrow(() -> new ControlCommandValidationException("Fanout retry record was not found."));
        ControlPlatformFanoutRetryRecord updated = success
                ? retryRecord.withState(ControlPlatformFanoutRetryState.SUCCEEDED, now, message)
                : retryRecord.withAttemptScheduled(now.plus(DEFAULT_RETRY_DELAY), now, message);
        return getControlPlatformFanoutRetryRepository().saveRetryRecord(updated);
    }
}
