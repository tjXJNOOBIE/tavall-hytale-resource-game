package org.tavall.control.cloud;

import org.tavall.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IRestoreJobCreationHandler extends IDependencyInjectableInterface {
    RestoreJob createAndCommand(UUID backupId, Optional<UUID> targetWorkloadId, Optional<UUID> targetNodeId,
                                UUID requestedBy, boolean approvalPresent, Instant now);
}
