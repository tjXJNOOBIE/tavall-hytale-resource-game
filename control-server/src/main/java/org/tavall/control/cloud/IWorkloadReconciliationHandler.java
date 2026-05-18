package org.tavall.control.cloud;

import com.tjxjnoobie.api.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface IWorkloadReconciliationHandler extends IDependencyInjectableInterface {
    WorkloadReconciliationDecision reconcile(UUID workloadId, UUID requestedBy, Instant now);
}
