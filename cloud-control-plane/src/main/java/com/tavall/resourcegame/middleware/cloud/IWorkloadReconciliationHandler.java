package com.tavall.resourcegame.middleware.cloud;

import com.tavall.resourcegame.dependency.IDependencyInjectableInterface;

import java.time.Instant;
import java.util.UUID;

public interface IWorkloadReconciliationHandler extends IDependencyInjectableInterface {
    WorkloadReconciliationDecision reconcile(UUID workloadId, UUID requestedBy, Instant now);
}
