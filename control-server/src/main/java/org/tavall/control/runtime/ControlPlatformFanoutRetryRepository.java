package org.tavall.control.runtime;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ControlPlatformFanoutRetryRepository {
    ControlPlatformFanoutRetryRecord saveRetryRecord(ControlPlatformFanoutRetryRecord retryRecord);

    Optional<ControlPlatformFanoutRetryRecord> findRetryRecord(UUID retryId);

    List<ControlPlatformFanoutRetryRecord> findRetriesForCommand(ControlCommandId commandId);

    List<ControlPlatformFanoutRetryRecord> findDueRetries(Instant now, int limit);
}
